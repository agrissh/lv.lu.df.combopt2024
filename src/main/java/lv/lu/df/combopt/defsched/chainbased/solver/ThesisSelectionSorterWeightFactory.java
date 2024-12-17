package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.chainbased.domain.Thesis;

public class ThesisSelectionSorterWeightFactory implements SelectionSorterWeightFactory<DefenseSchedule, Thesis> {
    @Override
    public Comparable createSorterWeight(DefenseSchedule defenseSchedule, Thesis thesis) {
        long weight = defenseSchedule.getThesis().stream()
                .filter(th -> th.getInvolved().stream().anyMatch(p -> thesis.getInvolved().contains(p)))
                .count();
        return new ThesisDifficultyWeight(thesis, weight);
    }
}
