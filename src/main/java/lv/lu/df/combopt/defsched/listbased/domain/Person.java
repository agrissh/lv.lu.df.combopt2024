package lv.lu.df.combopt.defsched.listbased.domain;

import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(scope = Person.class,
        property = "personId",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class Person {
    private Integer personId;
    private String name;

    @ProblemFactCollectionProperty
    private List<TimeConstraint> timeConstraints = new ArrayList<>();

    @Override
    public String toString() {
        return this.getName();
    }
}
