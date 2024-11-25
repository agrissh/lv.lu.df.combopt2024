package lv.lu.df.combopt.defsched.chainbased.domain;

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
@JsonIdentityInfo(scope = Program.class,
        property = "name",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class Program {
    private String name;
}
