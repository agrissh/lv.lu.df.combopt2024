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

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.loadBalance;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

public class CostFunction implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                authorUnavailable(constraintFactory),
                supervisorUnavailable(constraintFactory),
                reviewerUnavailable(constraintFactory),
                conflictingTimeForPerson(constraintFactory),
                sessionsForPerson(constraintFactory),
                secondSessionInOneDayForPerson(constraintFactory),
                fairSessions(constraintFactory),

                wrongRoleForMember(constraintFactory),
                notUniqueMember(constraintFactory)
        };
    }

    private Constraint authorUnavailable(ConstraintFactory constraintFactory) {
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
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (t, p) -> BigDecimal.valueOf(20))
                .asConstraint("Thesis with reviewer unavailable");
    }

    public Constraint conflictingTimeForPerson(ConstraintFactory constraintFactory) {
        // Iesaistītais netiek uz aizstāvēšanos arī tad, ja viņam ir jāveic pienākumi tanī pašā laikā citā sesijā.
        return constraintFactory
                .forEachUniquePair(Thesis.class)
                .filter((t1, t2) -> t1.overlapsWith(t2))
                .filter((t1, t2) -> t1.getSupervisor().equals(t2.getSupervisor()) ||
                        t1.getSupervisor().equals(t2.getReviewer()) ||
                        t1.getReviewer().equals(t2.getReviewer()) ||
                        t1.getReviewer().equals(t2.getSupervisor()))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Person has to attend two thesis' defence at once");
    }

    //// Vēlams, lai iesaistītajam būtu jāapmeklē pēc iespējas mazāk sesiju.
    public Constraint sessionsForPerson(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Person.class)
                .join(Thesis.class)
                .filter((p, t) -> t.getInvolved().contains(p))
                .join(Session.class)
                .filter((p, th, sess) -> sess.containsThesis(th))
                .map((p, th, sess) -> Pair.create(p, sess))
                .distinct()
                .groupBy(pair -> pair.getKey(), count())
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (person, count) -> BigDecimal.valueOf(count))
                .indictWith((person, count) -> List.of(person))
                .asConstraint("Session count for Person");
    }

    // Vēlams, lai iesaistītajam vienā dienā nav jāapmeklē vairākas sesijas.
    public Constraint secondSessionInOneDayForPerson(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Thesis.class, equal(th -> th.getSession().getSessionStart().toLocalDate()))
                .filter((th1, th2) -> !th1.getSession().equals(th2.getSession()))
                .join(Person.class)
                .filter((th1, th2, p) -> th1.getInvolved().contains(p) && th2.getInvolved().contains(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (th1, th2, p) -> BigDecimal.valueOf(5))
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
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Wrong Role");
    }

    // Komisijas locekļiem jābūt unikāliem!
    Constraint notUniqueMember(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(SessionMember.class)
                .filter((sm1,sm2) -> sm1.getAssignedMember().equals(sm2.getAssignedMember()))
                .join(Session.class)
                .filter((sm1,sm2,sess)->sess.getMembers().containsAll(List.of(sm1,sm2)))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Duplicate member");
    }


}