package lv.lu.df.combopt.defsched.chainbased.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class Member {
    private MemberRole role;
    private Boolean fromIndustry;
    private Program program;
}
