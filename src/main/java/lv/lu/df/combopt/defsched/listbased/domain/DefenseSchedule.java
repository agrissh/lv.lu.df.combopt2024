package lv.lu.df.combopt.defsched.listbased.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.*;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @PlanningEntityCollectionProperty
    private List<Session> sessions = new ArrayList<>();

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "theses")
    private List<Thesis> thesis = new ArrayList<>();

    //@ProblemFactCollectionProperty
    //private List<TimeConstraint> timeConstraints = new ArrayList<>();

    @PlanningScore
    private HardSoftScore score;

    private static final Logger LOGGER = LoggerFactory.getLogger(lv.lu.df.combopt.defsched.slotbased.domain.DefenseSchedule.class);
    public void printSchedule() {
        persons.forEach(p -> {
            LOGGER.info(p.getName() + " available at " + p.getTimeConstraints());
        });

        sessions.forEach(session -> {
            LOGGER.info("SESSION " + session.getSessionId() + " in room " + session.getRoom() + " starts at " +
                    session.getStartingAt());
            session.getThesisList().forEach(th -> {
                LOGGER.info("   Starts at: " + th.startsAt().toLocalTime().toString() + " " + th.getThesisId() +
                        " " + th.getAuthor().getName() + " " +
                        th.getTitle() + " vad. " + th.getSupervisor().getName() +
                        " rec. " + th.getReviewer().getName());
            });
        });
    }

}
