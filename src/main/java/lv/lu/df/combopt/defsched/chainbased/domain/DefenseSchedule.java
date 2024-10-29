package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PlanningSolution
public class DefenseSchedule {
    @PlanningId
    private Integer scheduleId;
    @ProblemFactCollectionProperty
    private List<Person> persons = new ArrayList<>();

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "sessions")
    private List<Session> sessions = new ArrayList<>();

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "theses")
    private List<Thesis> thesis = new ArrayList<>();

    @PlanningScore
    private HardMediumSoftScore score;

    private static final Logger LOGGER = LoggerFactory.getLogger(lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule.class);
    public void printSchedule() {
        persons.forEach(p -> {
            LOGGER.info(p.getName() + " not available at " + p.getTimeConstraints());
        });

        sessions.forEach(session -> {
            LOGGER.info("SESSION " + session.getSessionId() + " in room " + session.getRoom() + " starts at " +
                    session.startsAt());
            Thesis th = session.getNext();
            while (th != null) {
                LOGGER.info("   Starts at: " + th.getStartsAt().toLocalTime().toString() + /*" cascade: " +
                        th.getCascadeStartsAt().toLocalTime().toString() + */ " "+ th.getThesisId() +
                        " " + th.getAuthor().getName() + " " +
                        th.getTitle() + " vad. " + th.getSupervisor().getName() +
                        " rec. " + th.getReviewer().getName());
                th = th.getNext();
            }
        });
    }
}
