package lv.lu.df.combopt.defsched.listbased.solver;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.listbased.domain.Thesis;

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
            LocalDateTime time = thesis.getPrevious() != null && thesis.getPrevious().getStartsAt() != null ?
                    thesis.getPrevious().endsAt() : thesis.getSession().getStartingAt();
            Thesis shadowThesis = thesis;
            while (shadowThesis != null) {
                scoreDirector.beforeVariableChanged(shadowThesis, "startsAt");
                shadowThesis.setStartsAt(time);
                scoreDirector.afterVariableChanged(shadowThesis, "startsAt");
                time = time.plusMinutes(shadowThesis.getSession().getSlotDurationMinutes());
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
