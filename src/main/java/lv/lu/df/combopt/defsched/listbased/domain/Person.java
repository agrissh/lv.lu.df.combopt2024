package lv.lu.df.combopt.defsched.listbased.domain;

import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
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
