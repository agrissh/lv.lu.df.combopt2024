package lv.lu.df.combopt.defsched.listbased.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.*;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
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

    @JsonIdentityReference(alwaysAsId = false)
    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "theses")
    private List<Thesis> thesis = new ArrayList<>();

    //@ProblemFactCollectionProperty
    //private List<TimeConstraint> timeConstraints = new ArrayList<>();

    @PlanningScore
    private HardMediumSoftBigDecimalScore score;

    @ProblemFactProperty
    private ScheduleProperties properties = new ScheduleProperties();

    private static final Logger LOGGER = LoggerFactory.getLogger(lv.lu.df.combopt.defsched.slotbased.domain.DefenseSchedule.class);
    public void printSchedule() {
        persons.forEach(p -> {
            LOGGER.info(p.getName() + " not available at " + p.getTimeConstraints());
        });

        sessions.forEach(session -> {
            LOGGER.info("SESSION " + session.getSessionId() + " in room " + session.getRoom() + " starts at " +
                    session.getStartingAt());
            session.getThesisList().forEach(th -> {
                LOGGER.info("   Starts at: " + th.getStartsAt().toLocalTime().toString() + /*" cascade: " +
                        th.getCascadeStartsAt().toLocalTime().toString() + */ " "+ th.getThesisId() +
                        " " + th.getAuthor().getName() + " " +
                        th.getTitle() + " vad. " + th.getSupervisor().getName() +
                        " rec. " + th.getReviewer().getName());
            });
        });
    }

}
