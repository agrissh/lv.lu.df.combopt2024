package lv.lu.df.combopt.defsched.listbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import lv.lu.df.combopt.defsched.listbased.domain.Person;
import lv.lu.df.combopt.defsched.listbased.domain.Session;
import lv.lu.df.combopt.defsched.listbased.domain.Thesis;
import org.apache.commons.math3.util.Pair;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

public class ConstraintStreamCostFunction implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                authorUnavailable(constraintFactory),
                supervisorUnavailable(constraintFactory),
                reviewerUnavailable(constraintFactory),
                conflictingTimeForPerson(constraintFactory),
                sessionsForPerson(constraintFactory)

        };
    }

    public Constraint authorUnavailable(ConstraintFactory constraintFactory) {
        // Uz aizstāvēšanos jātiek autoram!
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getAuthor, p -> p))
                .filter((t,p) -> !t.isAvailable(p))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Thesis with author unavailable");
    }

    public Constraint supervisorUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba vadītājs
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getSupervisor, p -> p))
                .filter((t,p) -> !t.isAvailable(p))
                .penalize(HardSoftScore.ONE_SOFT, (t,p) -> 10)
                .asConstraint("Thesis with supervisor unavailable");
    }

    public Constraint reviewerUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba recenzents
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getReviewer, p -> p))
                .filter((t,p) -> !t.isAvailable(p))
                .penalize(HardSoftScore.ONE_SOFT, (t,p) -> 20)
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
                .penalize(HardSoftScore.ONE_HARD)
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
                .penalize(HardSoftScore.ONE_SOFT, (person, count) -> count)
                .asConstraint("Session count for Person");
    }

    // Vēlams, lai iesaistītajam vienā dienā nav jāapmeklē vairākas sesijas.

    // Nav tukši sloti starp diplomdarbiem

}
