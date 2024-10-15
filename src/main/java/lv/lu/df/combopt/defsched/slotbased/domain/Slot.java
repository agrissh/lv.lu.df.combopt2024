package lv.lu.df.combopt.defsched.slotbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Slot {
    private Integer slotId;
    private LocalDateTime start;
    private LocalDateTime end;
    public Boolean overlapsWith(Slot slot) {
        return !(this.getEnd().compareTo(slot.getStart()) <= 0 || this.getStart().compareTo(slot.getEnd()) >=0);
    }
}
