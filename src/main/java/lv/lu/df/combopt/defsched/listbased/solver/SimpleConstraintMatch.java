package lv.lu.df.combopt.defsched.listbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SimpleConstraintMatch {
    private String constraintName;
    private HardMediumSoftBigDecimalScore score;

    public SimpleConstraintMatch(ConstraintMatch<HardMediumSoftBigDecimalScore> constraintMatch) {
        this.constraintName = constraintMatch.getConstraintRef().constraintName();
        this.score = constraintMatch.getScore();
    }
}
