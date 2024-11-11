package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@PlanningEntity
public class SessionMember {

    @PlanningId
    private Integer seatId;
    @PlanningVariable(valueRangeProviderRefs = "members")
    private Member assignedMember;

    private MemberRole requiredRole;
}
