package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Session extends TimedEvent {
    @PlanningId
    private Integer sessionId;
    private String room;
    private Integer slotDurationMinutes;
    private LocalDateTime sessionStart;

    public LocalDateTime startsAt() {
        return sessionStart;
    }

    public LocalDateTime endsAt() {
        LocalDateTime endsAt = this.startsAt();
        Thesis next = this.getNext();
        while (next != null) {
            endsAt.plusMinutes(slotDurationMinutes);
            next = next.getNext();
        }
        return endsAt;
    }
}

