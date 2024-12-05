package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import lv.lu.df.combopt.defsched.chainbased.domain.*;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.*;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

public class CostFunction implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                //authorUnavailable(constraintFactory),
                //supervisorUnavailable(constraintFactory),
                //reviewerUnavailable(constraintFactory),
                //conflictingTimeForPerson(constraintFactory),
                sessionsForPerson(constraintFactory),
                //sessionsForPerson_new(constraintFactory)
                //*//sessionsForMember(constraintFactory)
                //secondSessionInOneDayForPerson(constraintFactory),
                //fairSessions(constraintFactory),

                //wrongRoleForMember(constraintFactory),
                //notUniqueMember(constraintFactory),
                //memberUnavailable(constraintFactory),
                //conflictingTimeForMember(constraintFactory),
                //conflictingSessionsForMember(constraintFactory)
        };
    }

    private Constraint authorUnavailable(ConstraintFactory constraintFactory) {
        // Autoram jātiek!!!!
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getAuthor, p -> p))
                .filter((t, p) -> !t.isAvailable(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Thesis with author unavailable");
    }

    public Constraint supervisorUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba vadītājs
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getSupervisor, p -> p))
                .filter((t, p) -> !t.isAvailable(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (t, p) -> BigDecimal.valueOf(10))
                .asConstraint("Thesis with supervisor unavailable");
    }

    public Constraint reviewerUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba recenzents
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getReviewer, p -> p))
                .filter((t, p) -> !t.isAvailable(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Thesis with reviewer unavailable");
    }

    public Constraint memberUnavailable(ConstraintFactory constraintFactory) {
        // Komisijas loceklim jātiek uz aizstāvēšanos!
        return constraintFactory
                .forEach(SessionMember.class)
                .join(Person.class, Joiners.filtering((sm,p)->p.getMembership().contains(sm.getAssignedMember())))
                .join(Session.class, Joiners.filtering((sm,p,sess)->sess.getMembers().contains(sm)))
                .join(Thesis.class, Joiners.filtering((sm,p,sess,th)->th.getSession().equals(sess) &&
                        !th.isAvailable(p)))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .indictWith((sm,p,sess,th)->List.of(sess, p, th))
                .asConstraint("Session with member unavailable");
    }

    public Constraint conflictingTimeForPerson(ConstraintFactory constraintFactory) {
        // Vadītājs vai recenzents netiek uz aizstāvēšanos arī tad, ja viņam ir jāveic šādi pienākumi tanī pašā laikā citā sesijā.
        return constraintFactory
                .forEachUniquePair(Thesis.class)
                .filter((t1, t2) -> t1.overlapsWith(t2))
                // TODO: Teorētiski var gadīties, ka vadītājs var aizstāvēt darbu citā programmā ...
                .filter((t1, t2) -> t1.getSupervisor().equals(t2.getSupervisor()) ||
                        t1.getSupervisor().equals(t2.getReviewer()) ||
                        t1.getReviewer().equals(t2.getReviewer()) ||
                        t1.getReviewer().equals(t2.getSupervisor()))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Person has to attend two thesis' defence at once");
    }

    public Constraint conflictingTimeForMember(ConstraintFactory constraintFactory) {
        // Komisijas loceklis netiek uz aizstāvēšanos arī tad, ja viņam ir jāveic recenzenta vai vadītāja tanī pašā laikā citā sesijā.
        return constraintFactory
                .forEach(Thesis.class)
                .join(Session.class, Joiners.filtering((th,sess) -> !th.getSession().equals(sess)
                        && th.overlapsWith(sess)))
                .join(SessionMember.class, Joiners.filtering((th,sess,sm) -> sess.getMembers().contains(sm)))
                .filter((th,sess,sm)->th.getInvolved().stream().anyMatch(p -> p.getMembership().stream().anyMatch(m -> m.equals(sm.getAssignedMember()))))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Member has to attend two thesis' defence at once");
    }

    public Constraint conflictingSessionsForMember(ConstraintFactory constraintFactory) {
        return constraintFactory
                // TODO: Jāpārbauda nevis Member ekvivalenci, bet gan Person (Ja ir vairākas komisijas)
                .forEachUniquePair(SessionMember.class, equal(SessionMember::getAssignedMember))
                .join(Session.class)
                .filter((sm1,sm2,sess)->sess.getMembers().contains(sm1))
                .ifExists(Session.class, Joiners.filtering((sm1,sm2,sess1,sess2)->!sess1.equals(sess2) && sess1.overlapsWith(sess2) && sess2.getMembers().contains(sm2)))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Parallel commissions");

    }

    //// Vēlams, lai iesaistītajam būtu jāapmeklē pēc iespējas mazāk sesiju.
    public Constraint sessionsForPerson(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Person.class)
                .join(Thesis.class, Joiners.filtering((p, t) -> t.getInvolved().contains(p)))
                .flattenLast(th -> List.of(th.getSession()))
                .distinct()
                .groupBy((p, sess)->p, countBi())
                .concat(constraintFactory
                   .forEach(Person.class)
                   .join(SessionMember.class, Joiners.filtering((p,sm)->p.getMembership().contains(sm.getAssignedMember())))
                   .join(Session.class, Joiners.filtering((p,sm,sess)->sess.getMembers().contains(sm)))
                   .groupBy((p,sm,sess) -> p, countTri())
                )
                .groupBy((person, count) -> person, sum((person, count) -> count))
                .filter((person, count) -> count > 2)
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (person, count) -> BigDecimal.valueOf(count * 25))
                .indictWith((person, count) -> List.of(person))
                .asConstraint("Session count for Person");
    }

    public Constraint sessionsForPerson_new(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Person.class)
                .join(Session.class)
                .ifExists(Thesis.class, Joiners.filtering((p, sess, th) -> th.getSession().equals(sess) && th.getInvolved().contains(p)))
                .groupBy((person, sess) -> person, countBi())

                //.groupBy((person, count) -> person, sum((person, count) -> count))
                .filter((person, count) -> count > 2)
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (person, count) -> BigDecimal.valueOf(count * 25))
                .indictWith((person, count) -> List.of(person))
                .asConstraint("Session count for Person");
    }

    public Constraint sessionsForMember(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Person.class)
                .join(SessionMember.class, Joiners.filtering((p,sm)->p.getMembership().contains(sm.getAssignedMember())))
                .join(Session.class, Joiners.filtering((p,sm,sess)->sess.getMembers().contains(sm)))
                .groupBy((p,sm,sess) -> p, countTri())
                .filter((person, count) -> count > 2)
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD,(person, count) -> BigDecimal.valueOf(count - 2))
                .indictWith((person, count) -> List.of(person))
                .asConstraint("Session count for Member");


    }

    // Vēlams, lai iesaistītajam vienā dienā nav jāapmeklē vairākas sesijas.
    public Constraint secondSessionInOneDayForPerson(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Thesis.class,
                        Joiners.equal(th -> th.getSession().getSessionStart().toLocalDate()),
                        Joiners.filtering((th1, th2) -> !th1.getSession().equals(th2.getSession())))
                .join(Person.class, Joiners.filtering((th1, th2, p) -> th1.getInvolved().contains(p) && th2.getInvolved().contains(p)))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (th1, th2, p) -> BigDecimal.valueOf(15))
                .asConstraint("Second Session in one day for Person");
    }

    // Diplomdarbu skaits ir sabalansēts starp sesijām
    Constraint fairSessions(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Thesis.class)
                .groupBy(Thesis::getSession, count())
                .complement(Session.class, sess -> 0)
                .groupBy(loadBalance((sess, c) -> sess, (sess, c) -> c))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_MEDIUM, LoadBalance::unfairness)
                .asConstraint("Fair Sessions");
    }

    // Sesijā jābūt priekšniekam un sekretāram!
    Constraint wrongRoleForMember(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(SessionMember.class)
                .join(Member.class, equal(SessionMember::getAssignedMember, m -> m))
                .filter((sm,m)->sm.getRequiredRole() != null && !sm.getRequiredRole().equals(m.getRole()))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD, (sm,m) -> BigDecimal.valueOf(10))
                .asConstraint("Wrong Role");
    }

    // Komisijas locekļiem jābūt unikāliem!
    Constraint notUniqueMember(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(SessionMember.class)
                .filter((sm1,sm2) -> sm1.getAssignedMember().equals(sm2.getAssignedMember()))
                .join(Session.class)
                .filter((sm1,sm2,sess)->sess.getMembers().containsAll(List.of(sm1,sm2)))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD, (sm1,sm2,sess) -> BigDecimal.valueOf(10))
                .asConstraint("Duplicate member");
    }


}