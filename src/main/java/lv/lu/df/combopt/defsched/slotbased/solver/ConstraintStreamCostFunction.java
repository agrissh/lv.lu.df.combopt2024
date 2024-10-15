package lv.lu.df.combopt.defsched.slotbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import lv.lu.df.combopt.defsched.slotbased.domain.Person;
import lv.lu.df.combopt.defsched.slotbased.domain.Session;
import lv.lu.df.combopt.defsched.slotbased.domain.Thesis;
import org.apache.commons.math3.util.Pair;


import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countTri;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

public class ConstraintStreamCostFunction implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                everyThesis(constraintFactory),
                thesisInTheSameSlot(constraintFactory),
                authorUnavailable(constraintFactory),
                supervisorUnavailable(constraintFactory),
                reviewerUnavailable(constraintFactory),
                conflictingSlotsForPerson(constraintFactory),
                sessionsForPerson(constraintFactory)

        };
    }

    public Constraint everyThesis(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Thesis.class)
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("every Thesis");
    }

    public Constraint thesisInTheSameSlot(ConstraintFactory constraintFactory) {
        // Darbi nav ieplānoti vienlaicīgi (vienā laika slotā)!
        return constraintFactory
                .forEachUniquePair(Thesis.class, equal(Thesis::getDefenseSlot))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Thesis in the same Slot");
    }

    public Constraint authorUnavailable(ConstraintFactory constraintFactory) {
        // Darbi nav ieplānoti vienlaicīgi (vienā laika slotā)!
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getAuthor, p -> p))
                .filter((t,p) -> !p.getAvailableSlots().contains(t.getDefenseSlot()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Thesis with author unavailable");
    }

    public Constraint supervisorUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba vadītājs
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getSupervisor, p -> p))
                .filter((t,p) -> !p.getAvailableSlots().contains(t.getDefenseSlot()))
                .penalize(HardSoftScore.ONE_SOFT, (t,p) -> 10)
                .asConstraint("Thesis with supervisor unavailable");
    }

    public Constraint reviewerUnavailable(ConstraintFactory constraintFactory) {
        // Vēlams, ka uz aizstāvēšanos tiek darba recenzents
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getReviewer, p -> p))
                .filter((t,p) -> !p.getAvailableSlots().contains(t.getDefenseSlot()))
                .penalize(HardSoftScore.ONE_SOFT, (t,p) -> 20)
                .asConstraint("Thesis with reviewer unavailable");
    }

    public Constraint conflictingSlotsForPerson(ConstraintFactory constraintFactory) {
        // Iesaistītais netiek uz aizstāvēšanos arī tad, ja viņam ir jāveic pienākumi tanī pašā laikā citā sesijā.
        return constraintFactory
                .forEachUniquePair(Thesis.class)
                .filter((t1, t2) -> t1.getDefenseSlot().overlapsWith(t2.getDefenseSlot()))
                .filter((t1, t2) -> t1.getSupervisor().equals(t2.getSupervisor()) ||
                        t1.getSupervisor().equals(t2.getReviewer()) ||
                        t1.getReviewer().equals(t2.getReviewer()) ||
                        t1.getReviewer().equals(t2.getSupervisor()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Person has to attend two slots at once");
    }

    //// Vēlams, lai iesaistītajam būtu jāapmeklē pēc iespējas mazāk sesiju.
    public Constraint sessionsForPerson(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Person.class)
                .join(Thesis.class)
                .filter((p,t) -> t.getInvolved().contains(p))
                .join(Session.class)
                .filter((p,th,sess) -> sess.getSlots().contains(th.getDefenseSlot()))
                .map((p,th,sess) -> Pair.create(p,sess))
                .distinct()
                .groupBy(pair -> pair.getKey(), count())
                .penalize(HardSoftScore.ONE_SOFT, (person, count) -> count)
                .asConstraint("Session count for Person");
    }

    // Vēlams, lai iesaistītajam vienā dienā nav jāapmeklē vairākas sesijas.

    // Nav tukši sloti starp diplomdarbiem

}
