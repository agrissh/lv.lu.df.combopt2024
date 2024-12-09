package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Thesis.class, name = "thesis"),
        @JsonSubTypes.Type(value = Session.class, name = "session")
})

public abstract class TimedEvent {
    @JsonIdentityReference(alwaysAsId = true)
    @InverseRelationShadowVariable(sourceVariableName = "prev")
    Thesis next;

    public LocalDateTime startsAt() {
        return null;
    };

    public LocalDateTime endsAt() {
        return null;
    }

    public Boolean overlapsWith(TimedEvent event) {
        return this.endsAt()!=null && event.endsAt()!=null &&
                !(this.endsAt().compareTo(event.startsAt()) <= 0 || this.startsAt().compareTo(event.endsAt()) >=0);
    }
}
