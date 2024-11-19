package lv.lu.df.combopt.defsched.chainbased.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeConstraint {
    private Integer timeConstraintId;
    private LocalDateTime from;
    private LocalDateTime to;

    @Override
    public String toString() {
        return this.getFrom().toString() + " -- " + this.getTo().toString();
    }
}