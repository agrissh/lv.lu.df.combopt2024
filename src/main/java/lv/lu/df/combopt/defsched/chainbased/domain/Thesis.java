package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lv.lu.df.combopt.defsched.chainbased.solver.PlanningVariableChangeListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PlanningEntity
@JsonIdentityInfo(scope = lv.lu.df.combopt.defsched.chainbased.domain.Thesis.class,
        property = "thesisId",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class Thesis extends TimedEvent {
    @PlanningId
    private Integer thesisId;
    private String title;

    @JsonIdentityReference(alwaysAsId = true)
    private Person author;
    @JsonIdentityReference(alwaysAsId = true)
    private Person supervisor;
    @JsonIdentityReference(alwaysAsId = true)
    private Person reviewer;

    @PlanningVariable(graphType = PlanningVariableGraphType.CHAINED,
            valueRangeProviderRefs = {"sessions", "theses"})
    @JsonIdentityReference(alwaysAsId = true)
    TimedEvent prev;

    @AnchorShadowVariable(sourceVariableName = "prev")
    @JsonIdentityReference(alwaysAsId = true)
    Session session;

    //@ShadowVariable(variableListenerClass = PlanningVariableChangeListener.class,
    //        sourceVariableName = "session")
    @ShadowVariable(variableListenerClass = PlanningVariableChangeListener.class,
	            sourceVariableName = "prev")
	private LocalDateTime startsAt;

    @JsonIdentityReference(alwaysAsId = true)
    private Program program;

    private void updateStartTime() {
        if (this.getPrev() == null) {
            this.startsAt = null;
        } else {
            this.startsAt = this.getPrev() instanceof Session ? this.getPrev().startsAt() : this.getPrev().endsAt();
        }
    }

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

    @JsonIgnore
    public Boolean isAvailable(Person person) {
        return !person.getTimeConstraints().stream().anyMatch(tc ->
                this.getPrev() != null &&
                        !(this.endsAt().compareTo(tc.getFrom()) <= 0
                                || this.startsAt().compareTo(tc.getTo()) >=0));
    }
    @JsonIgnore
    public List<Person> getInvolved() {
        return List.of(author, supervisor, reviewer);
    }

    public String toString() {
        return this.title;
    }
}
