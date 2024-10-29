package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.chainbased.domain.Thesis;

import java.time.LocalDateTime;

public class PlanningVariableChangeListener implements VariableListener<DefenseSchedule, Thesis> {
    @Override
    public void beforeVariableChanged(ScoreDirector<DefenseSchedule> scoreDirector, Thesis thesis) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector<DefenseSchedule> scoreDirector, Thesis thesis) {
        if (thesis.getSession() == null) {
            scoreDirector.beforeVariableChanged(thesis, "startsAt");
            thesis.setStartsAt(null);
            scoreDirector.afterVariableChanged(thesis, "startsAt");
        } else {
            LocalDateTime time = thesis.getPrev() != null && thesis.getPrev().startsAt() != null ?
                    thesis.getPrev().endsAt() : thesis.getSession().startsAt();
            Thesis shadowThesis = thesis;
            while (shadowThesis != null) {
                scoreDirector.beforeVariableChanged(shadowThesis, "startsAt");
                shadowThesis.setStartsAt(time);
                scoreDirector.afterVariableChanged(shadowThesis, "startsAt");
                time = shadowThesis.endsAt();
                shadowThesis = shadowThesis.getNext();
            }
        }
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<DefenseSchedule> scoreDirector, Thesis thesis) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector<DefenseSchedule> scoreDirector, Thesis thesis) {

    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<DefenseSchedule> scoreDirector, Thesis thesis) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector<DefenseSchedule> scoreDirector, Thesis thesis) {

    }
}
