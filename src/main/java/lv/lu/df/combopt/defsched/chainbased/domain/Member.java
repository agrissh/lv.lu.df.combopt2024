package lv.lu.df.combopt.defsched.chainbased.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
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
@JsonIdentityInfo(scope = lv.lu.df.combopt.defsched.chainbased.domain.Member.class,
        property = "memberId",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class Member {
    private Integer memberId;
    private MemberRole role;
    private Boolean fromIndustry;
    @JsonIdentityReference(alwaysAsId = true)
    private Program program;
}
