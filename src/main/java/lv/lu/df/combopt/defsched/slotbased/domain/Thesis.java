package lv.lu.df.combopt.defsched.slotbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@PlanningEntity
public class Thesis {
    @PlanningId
    private Integer thesisId;
    private String title;
    @PlanningVariable(valueRangeProviderRefs = "slots")
    private Slot defenseSlot;
    private Person author;
    private Person supervisor;
    private Person reviewer;

    public List<Person> getInvolved() {
        return List.of(author, supervisor, reviewer);
    }

    @Override
    public String toString() {
        return this.title;
    }
}
