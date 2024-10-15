package lv.lu.df.combopt.defsched.slotbased.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@PlanningSolution
@ToString
public class DefenseSchedule {

    @PlanningId
    private Integer scheduleId;
    @ProblemFactCollectionProperty
    private List<Person> persons = new ArrayList<>();
    @ProblemFactCollectionProperty
    private List<Session> sessions = new ArrayList<>();
    @PlanningEntityCollectionProperty
    private List<Thesis> thesis = new ArrayList<>();
    @ValueRangeProvider(id = "slots")
    private List<Slot> slots = new ArrayList<>();

    @PlanningScore
    //private SimpleScore score;
    private HardSoftScore score;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefenseSchedule.class);
    public void printSchedule() {
        persons.forEach(p -> {
            LOGGER.info(p.getName() + " available at " + p.getAvailableSlots().stream().map(s -> s.getSlotId()).collect(Collectors.toList()));
        });

        sessions.forEach(session -> {
            LOGGER.info("SESSION " + session.getSessionId() + " in room " + session.getRoom() + " starts at " +
                    session.getStartingAt());
            session.getSlots().forEach(slot -> {
                LOGGER.info("   SLOT " + slot.getSlotId() + " starts at " + slot.getStart().toLocalTime() +
                        ", ends at " + slot.getEnd().toLocalTime());
                thesis.stream().filter(thesis -> thesis.getDefenseSlot() != null && thesis.getDefenseSlot().equals(slot)).forEach(th ->
                        LOGGER.info("      THESIS "+ th.getThesisId() + " " + th.getAuthor().getName() + " " +
                                th.getTitle() + " vad. " + th.getSupervisor().getName() +
                                " rec. " + th.getReviewer().getName()));
            });
        });
    }
}
