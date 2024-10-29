package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.AnchorShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lv.lu.df.combopt.defsched.chainbased.solver.PlanningVariableChangeListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PlanningEntity
public class Thesis extends TimedEvent {
    @PlanningId
    private Integer thesisId;
    private String title;

    private Person author;
    private Person supervisor;
    private Person reviewer;

    @PlanningVariable(graphType = PlanningVariableGraphType.CHAINED,
            valueRangeProviderRefs = {"sessions", "theses"})
    TimedEvent prev;

    @AnchorShadowVariable(sourceVariableName = "prev")
    Session session;

    //@ShadowVariable(variableListenerClass = PlanningVariableChangeListener.class,
    //        sourceVariableName = "session")
    @ShadowVariable(variableListenerClass = PlanningVariableChangeListener.class,
            sourceVariableName = "prev")
    private LocalDateTime startsAt;

    public LocalDateTime startsAt() {
        return this.getStartsAt();
    }

    public LocalDateTime endsAt() {
        if (this.getPrev() == null) {
            return null;
        } else {
            return this.startsAt().plusMinutes(this.getSession().getSlotDurationMinutes());
        }
    }
    public Boolean isAvailable(Person person) {
        return !person.getTimeConstraints().stream().anyMatch(tc ->
                this.getPrev() != null &&
                        !(this.endsAt().compareTo(tc.getFrom()) <= 0
                                || this.startsAt().compareTo(tc.getTo()) >=0));
    }
}
