package lv.lu.df.combopt.defsched.listbased.domain;

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
import lv.lu.df.combopt.defsched.listbased.solver.PlanningVariableChangeListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PlanningEntity
@JsonIdentityInfo(scope = Thesis.class,
        property = "thesisId",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class Thesis {
    @PlanningId
    private Integer thesisId;
    private String title;

    @JsonIdentityReference(alwaysAsId = true)
    private Person author;
    @JsonIdentityReference(alwaysAsId = true)
    private Person supervisor;
    @JsonIdentityReference(alwaysAsId = true)
    private Person reviewer;

    @JsonIdentityReference(alwaysAsId = true)
    @InverseRelationShadowVariable(sourceVariableName = "thesisList")
    private Session session;

    @JsonIdentityReference(alwaysAsId = true)
    @PreviousElementShadowVariable(sourceVariableName = "thesisList")
    private Thesis previous;

    @JsonIdentityReference(alwaysAsId = true)
    @NextElementShadowVariable(sourceVariableName = "thesisList")
    private Thesis next;

    @ShadowVariable(variableListenerClass = PlanningVariableChangeListener.class,
             sourceVariableName = "session")
    @ShadowVariable(variableListenerClass = PlanningVariableChangeListener.class,
            sourceVariableName = "previous")
    private LocalDateTime startsAt = null;

    @CascadingUpdateShadowVariable(targetMethodName = "updateStartsAt")
    private LocalDateTime cascadeStartsAt = null;

    public void updateStartsAt() {
        if (this.getSession() == null) {
            this.setCascadeStartsAt(null);
        } else {
            this.setCascadeStartsAt(
                    this.getPrevious() == null ? this.getSession().getStartingAt() :
                            this.getPrevious().getCascadeStartsAt().plusMinutes(this.getSession().getSlotDurationMinutes())
            );
        }
    }

    public Boolean isAvailable(Person person) {
        return !person.getTimeConstraints().stream().anyMatch(tc ->
                this.getSession() != null &&
                        !(this.endsAt().compareTo(tc.getFrom()) <= 0
                                || this.getStartsAt().compareTo(tc.getTo()) >=0));
    }

    @JsonIgnore
    public List<Person> getInvolved() {
        return List.of(author, supervisor, reviewer);
    }

    public LocalDateTime startsAt() {
        if (this.getSession() == null) {
            return null;
        } else {
            LocalDateTime time = this.getSession().getStartingAt();
            Thesis th = this.getPrevious();
            while (th != null) {
                time = time.plusMinutes(this.getSession().getSlotDurationMinutes());
                th = th.getPrevious();
            }
            return time;
        }
    }

    public LocalDateTime endsAt() {
        if (this.getSession() == null) {
            return null;
        } else {
           return this.getStartsAt().plusMinutes(this.getSession().getSlotDurationMinutes());
        }
    }

    public Boolean overlapsWith(Thesis th) {
        return !(this.endsAt().compareTo(th.getStartsAt()) <= 0 || this.getStartsAt().compareTo(th.endsAt()) >=0);
    }

    @Override
    public String toString() {
        return this.title;
    }
}
