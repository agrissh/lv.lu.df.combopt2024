package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
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

    public LocalDateTime endsAt() {
        LocalDateTime endsAt = this.startsAt();
        Thesis next = this.getNext();
        while (next != null) {
            endsAt = endsAt.plusMinutes(slotDurationMinutes);
            next = next.getNext();
        }
        return endsAt;
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

