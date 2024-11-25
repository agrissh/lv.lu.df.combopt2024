package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lv.lu.df.combopt.defsched.listbased.domain.Person;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@PlanningEntity
@JsonIdentityInfo(scope = SessionMember.class,
        property = "seatId",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class SessionMember {

    @PlanningId
    private Integer seatId;
    @PlanningVariable(valueRangeProviderRefs = "members")
    private Member assignedMember;

    private MemberRole requiredRole;
}
