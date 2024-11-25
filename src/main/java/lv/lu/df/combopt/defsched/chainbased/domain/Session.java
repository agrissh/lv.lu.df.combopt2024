package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.variable.PiggybackShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lv.lu.df.combopt.defsched.chainbased.solver.PlanningVariableChangeListenerNext;
import lv.lu.df.combopt.defsched.listbased.domain.Person;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@PlanningEntity
@JsonIdentityInfo(scope = Session.class,
        property = "sessionId",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class Session extends TimedEvent {
    @PlanningId
    private Integer sessionId;
    private String room;
    private Integer slotDurationMinutes;
    private LocalDateTime sessionStart;

    private List<SessionMember> members = new ArrayList<>();

    public LocalDateTime startsAt() {
        return sessionStart;
    }

    @ShadowVariable(variableListenerClass = PlanningVariableChangeListenerNext.class, sourceEntityClass = TimedEvent.class,
    sourceVariableName = "next")
    private LocalDateTime endingTime;

    public LocalDateTime endsAt() {
        /*LocalDateTime endsAt = this.startsAt();
        Thesis next = this.getNext();
        while (next != null) {
            endsAt = endsAt.plusMinutes(slotDurationMinutes);
            next = next.getNext();
        }*/
        return endingTime;
    }

    public Boolean containsThesis(Thesis th) {
        Thesis it = this.getNext();
        while (it != null) {
            if (th.equals(it)) return true;
            it = it.getNext();
        }
        return false;
    }

    public List<Member> members() {
        return this.getMembers().stream().map(SessionMember::getAssignedMember).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return this.getSessionStart().toString() + " " + this.getRoom();
    }
}

