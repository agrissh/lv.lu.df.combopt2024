package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import lv.lu.df.combopt.defsched.chainbased.domain.Person;
import lv.lu.df.combopt.defsched.chainbased.domain.Thesis;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

public class CostFunction implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                authorUnavailable(constraintFactory),
                //supervisorUnavailable(constraintFactory),
                //reviewerUnavailable(constraintFactory),
                //conflictingTimeForPerson(constraintFactory),
                //sessionsForPerson(constraintFactory),
                //secondSessionInOneDayForPerson(constraintFactory),
                //fairSessions(constraintFactory),
                //fairSessionsProp(constraintFactory)
        };
    }

    private Constraint authorUnavailable(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Thesis.class)
                .join(Person.class, equal(Thesis::getAuthor, p -> p))
                .filter((t,p) -> !t.isAvailable(p))
                .penalizeBigDecimal(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Thesis with author unavailable");
    }
}
