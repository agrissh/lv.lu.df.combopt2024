package lv.lu.df.combopt.defsched.slotbased.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Person {
    private Integer personId;
    private String name;
    private List<Slot> availableSlots = new ArrayList<>();

    @Override
    public String toString() {
        return this.getName();
    }
}
