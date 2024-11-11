package lv.lu.df.combopt.defsched.listbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.*;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;
import lv.lu.df.combopt.defsched.listbased.domain.Person;
import lv.lu.df.combopt.defsched.listbased.domain.ScheduleProperties;
import lv.lu.df.combopt.defsched.listbased.domain.Session;
import lv.lu.df.combopt.defsched.listbased.domain.Thesis;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.*;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

public class ConstraintStreamCostFunction implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                authorUnavailable(constraintFactory),
                supervisorUnavailable(constraintFactory),
                reviewerUnavailable(constraintFactory),
                conflictingTimeForPerson(constraintFactory),
                sessionsForPerson(constraintFactory),
                secondSessionInOneDayForPerson(constraintFactory),
                fairSessions(constraintFactory),
                //fairSessionsProp(constraintFactory)
        };
    }

    public Constraint authorUnavailable(ConstraintFactory constraintFactory) {
        // Uz aizstāvēšanos jātiek autoram!
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getAuthor, p -> p))
                .filter((t,p) -> !t.isAvailable(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Thesis with author unavailable");
    }

    public Constraint supervisorUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba vadītājs
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getSupervisor, p -> p))
                .filter((t,p) -> !t.isAvailable(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (t,p) -> BigDecimal.valueOf(10))
                .asConstraint("Thesis with supervisor unavailable");
    }

    public Constraint reviewerUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba recenzents
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getReviewer, p -> p))
                .filter((t,p) -> !t.isAvailable(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (t,p) -> BigDecimal.valueOf(20))
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
                .filter((p,t) -> t.getInvolved().contains(p))
                .join(Session.class)
                .filter((p,th,sess) -> sess.getThesisList().contains(th))
                .map((p,th,sess) -> Pair.create(p,sess))
                .distinct()
                .groupBy(pair -> pair.getKey(), count())
                .filter((p,c)->c>1)
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (person, count) -> BigDecimal.valueOf(count))
                .indictWith((person, count) -> List.of(person))
                .asConstraint("Session count for Person");
    }

    // Vēlams, lai iesaistītajam vienā dienā nav jāapmeklē vairākas sesijas.
    public Constraint secondSessionInOneDayForPerson(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Thesis.class, equal(th -> th.getSession().getStartingAt().toLocalDate()))
                .filter((th1, th2) -> !th1.getSession().equals(th2.getSession()))
                .join(Person.class)
                .filter((th1,th2,p) -> th1.getInvolved().contains(p) && th2.getInvolved().contains(p))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, (th1,th2,p)->BigDecimal.valueOf(5))
                .asConstraint("Second Session in one day for Person");
    }

    // Diplomdarbu skaits ir sabalansēts starp sesijām
    Constraint fairSessions(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Thesis.class)
                .groupBy(Thesis::getSession, count())
                .complement(Session.class, sess -> 0)
                .groupBy(loadBalance((sess,c)->sess, (sess,c)->c))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_MEDIUM, LoadBalance::unfairness)
                .asConstraint("Fair Sessions");
    }

    Constraint fairSessionsProp(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Thesis.class)
                .groupBy(Thesis::getSession, count())
                .complement(Session.class, sess -> 0)
                .join(ScheduleProperties.class)
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_MEDIUM, (sess, count, props) ->
                        BigDecimal.valueOf(Math.sqrt(Math.pow(count - props.getSessionSize(), 2))))
                .indictWith((sess, count, props) -> List.of(sess))
                .asConstraint("Fair Sessions From Props");
    }
}