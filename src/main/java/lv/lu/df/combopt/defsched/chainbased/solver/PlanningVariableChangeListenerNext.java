package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.chainbased.domain.Session;
import lv.lu.df.combopt.defsched.chainbased.domain.Thesis;
import lv.lu.df.combopt.defsched.chainbased.domain.TimedEvent;

import java.time.LocalDateTime;

public class PlanningVariableChangeListenerNext implements VariableListener<DefenseSchedule, TimedEvent> {
    @Override
    public void beforeVariableChanged(ScoreDirector<DefenseSchedule> scoreDirector, TimedEvent timedEvent) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector<DefenseSchedule> scoreDirector, TimedEvent timedEvent) {

        Session session = timedEvent instanceof Session ? (Session) timedEvent : ((Thesis) timedEvent).getSession();
        if (session == null) {
            //what we can do???
        } else {
            LocalDateTime time = timedEvent instanceof Session ?
                    timedEvent.startsAt() :
                    timedEvent.endsAt();
            Thesis shadowThesis = timedEvent instanceof Session ?
                    timedEvent.getNext():
                    (Thesis) timedEvent;
            while (shadowThesis != null) {
                time = shadowThesis.endsAt();
                shadowThesis = shadowThesis.getNext();
            }
            scoreDirector.beforeVariableChanged(session, "endingTime");
            session.setEndingTime(time);
            scoreDirector.afterVariableChanged(session, "endingTime");
        }
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<DefenseSchedule> scoreDirector,  TimedEvent timedEvent) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector<DefenseSchedule> scoreDirector,  TimedEvent timedEvent) {

    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<DefenseSchedule> scoreDirector,  TimedEvent timedEvent) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector<DefenseSchedule> scoreDirector,  TimedEvent timedEvent) {

    }
}
