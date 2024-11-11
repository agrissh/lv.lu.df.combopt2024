package lv.lu.df.combopt.defsched.listbased.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import lombok.Getter;
import lombok.Setter;
import lv.lu.df.combopt.defsched.listbased.domain.Person;
import lv.lu.df.combopt.defsched.listbased.domain.Session;
import lv.lu.df.combopt.defsched.listbased.domain.Thesis;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter @Getter
public class SimpleIndictmentObject {
    private Integer indictedObjectID;
    private String indictedObjectClass;
    private HardMediumSoftBigDecimalScore score;
    private int matchCount;
    private List<SimpleConstraintMatch> constraintMatches = new ArrayList<>();

    public SimpleIndictmentObject(Object indictedObject, HardMediumSoftBigDecimalScore score, int matchCount, Set<ConstraintMatch<HardMediumSoftBigDecimalScore>> constraintMatches) {
        this.indictedObjectID = indictedObject instanceof Person ? ((Person) indictedObject).getPersonId() :
                indictedObject instanceof Thesis ? ((Thesis) indictedObject).getThesisId() :
                        indictedObject instanceof Session ? ((Session) indictedObject).getSessionId() :
                                0;
        this.indictedObjectClass = indictedObject instanceof Person ? "Person" :
                indictedObject instanceof Thesis ? "Thesis" :
                        indictedObject instanceof Session ? "Session" :
                                "LoadBalancer";
        this.score = score;
        this.matchCount = matchCount;
        this.constraintMatches = constraintMatches.stream().map(constraintMatch -> {
            return new SimpleConstraintMatch(constraintMatch);
        }).collect(Collectors.toList());
    }
}
