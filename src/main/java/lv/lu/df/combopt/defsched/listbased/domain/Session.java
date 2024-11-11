package lv.lu.df.combopt.defsched.listbased.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(scope = Session.class,
        property = "sessionId",
        generator = ObjectIdGenerators.PropertyGenerator.class)
@PlanningEntity
public class Session {
    @PlanningId
    private Integer sessionId;
    private String room;
    private LocalDateTime startingAt;
    private Integer slotDurationMinutes;

    @PlanningListVariable(valueRangeProviderRefs = "theses")
    private List<Thesis> thesisList = new ArrayList<>();
    @Override
    public String toString() {
        return this.getStartingAt().toString() + " " + this.getRoom();
    }

}
