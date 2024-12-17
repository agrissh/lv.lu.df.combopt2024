package lv.lu.df.combopt.defsched.chainbased.solver;

import lv.lu.df.combopt.defsched.chainbased.domain.Thesis;
import org.apache.commons.lang3.builder.CompareToBuilder;

public class ThesisDifficultyWeight implements Comparable<ThesisDifficultyWeight> {

    public final Thesis thesis;
    public final long weight;

    public ThesisDifficultyWeight(Thesis thesis, long weight) {
        this.thesis = thesis;
        this.weight = weight;
    }

    @Override
    public int compareTo(ThesisDifficultyWeight arg) {
        return new CompareToBuilder()
                .append(arg.weight, this.weight)
                .append(arg.thesis.getThesisId(), this.thesis.getThesisId())
                .toComparison();
    }
}
