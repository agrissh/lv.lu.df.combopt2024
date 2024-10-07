package lv.lu.df.combopt.defsched.slotbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import lv.lu.df.combopt.defsched.slotbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.slotbased.domain.Thesis;

public class EasyCostFunction implements EasyScoreCalculator<DefenseSchedule, SimpleScore> {
    @Override
    public SimpleScore calculateScore(DefenseSchedule defenseSchedule) {
        int cost = 0;

        for (Thesis th : defenseSchedule.getThesis()) {
            if (th.getDefenseSlot() == null) {
                cost = cost + 100;
            } else {
                if (!th.getAuthor().getAvailableSlots().contains(th.getDefenseSlot())) { cost = cost + 10; }
                if (!th.getSupervisor().getAvailableSlots().contains(th.getDefenseSlot())) { cost = cost + 10; }
                if (!th.getReviewer().getAvailableSlots().contains(th.getDefenseSlot())) { cost = cost + 10; }

                if (defenseSchedule.getThesis().stream().anyMatch(th2 -> !th2.equals(th) && th2.getDefenseSlot() != null
                        && th2.getDefenseSlot().equals(th.getDefenseSlot()))) { cost = cost + 50; }

            }
        }
        return SimpleScore.of(- cost);
    }
}
