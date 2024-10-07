package lv.lu.df.combopt.defsched.slotbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@PlanningEntity
@ToString
public class Thesis {
    private Integer thesisId;
    private String title;
    @PlanningVariable(valueRangeProviderRefs = "slots")
    private Slot defenseSlot;
    private Person author;
    private Person supervisor;
    private Person reviewer;
}
