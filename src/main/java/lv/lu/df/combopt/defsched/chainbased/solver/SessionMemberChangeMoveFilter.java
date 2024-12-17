package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.ChangeMove;
import lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.chainbased.domain.Member;
import lv.lu.df.combopt.defsched.chainbased.domain.SessionMember;

public class SessionMemberChangeMoveFilter implements SelectionFilter<DefenseSchedule, ChangeMove> {
    @Override
    public boolean accept(ScoreDirector<DefenseSchedule> scoreDirector, ChangeMove move) {
        SessionMember sm = (SessionMember) (move.getEntity());
        Member m = (Member) (move.getToPlanningValue());
        return (sm.getRequiredRole()==null || sm.getRequiredRole().equals(m.getRole()))
                && !sm.getSession().getMembers().contains(m);
    }
}
