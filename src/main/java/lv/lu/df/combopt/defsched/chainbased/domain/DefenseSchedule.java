package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.*;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
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

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "members")
    @JsonIdentityReference(alwaysAsId = true)
    public List<Member> members = new ArrayList<>();

    @ProblemFactCollectionProperty
    private List<Program> programs = new ArrayList<>();

    @PlanningEntityCollectionProperty
    @JsonIdentityReference(alwaysAsId = true)
    private List<SessionMember> sessionMembers = new ArrayList<>();

    @PlanningScore
    private HardMediumSoftBigDecimalScore score;

    //ConstraintWeightOverrides<HardMediumSoftBigDecimalScore> constraintWeightOverrides;

    private static final Logger LOGGER = LoggerFactory.getLogger(lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule.class);
    public void printSchedule() {
        persons.forEach(p -> {
            LOGGER.info(p.getName() + " not available at " + p.getTimeConstraints());
        });

        sessions.forEach(session -> {
            LOGGER.info("SESSION " + session.getSessionId() + " in room " + session.getRoom() + " starts at " +
                    session.startsAt() + " && ends at " + session.endsAt());
            session.getMembers().stream().forEach(m -> {
                Person person = this.getPersons().stream().filter(p -> p.getMembership().contains(m.getAssignedMember())).findFirst().get();
                LOGGER.info("   " + m.getRequiredRole() + " : " + person.getName() + " " + m.getAssignedMember().getRole() + " " +
                        (m.getAssignedMember().getFromIndustry() ? "I" : "A"));
            });
            Thesis th = session.getNext();
            while (th != null) {
                LOGGER.info("   Starts at: " + th.getStartsAt().toLocalTime().toString() + /*" cascade: " +
                        th.getCascadeStartsAt().toLocalTime().toString() + */ " "+ th.getThesisId() +
                        " " + th.getAuthor().getName() + " " +
                        th.getTitle() + " ("+ th.getProgram().getName() + ")" + " vad. " + th.getSupervisor().getName() +
                        " rec. " + th.getReviewer().getName());
                th = th.getNext();
            }
        });
    }
}
