package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.heuristic.move.AbstractMove;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import lv.lu.df.combopt.defsched.chainbased.domain.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ChangeChiefCommissionMove extends AbstractMove<DefenseSchedule> {
    private Session session;
    private Member new_chief;
    private Member old_chief;
    public ChangeChiefCommissionMove(Session session, Member chief) {
        this.session = session;
        this.new_chief = chief;
        this.old_chief = session.getMembers().stream()
                .filter(sm -> sm.getRequiredRole().equals(MemberRole.CHIEF))
                .map(sm -> sm.getAssignedMember())
                .findFirst().get();
    }
    @Override
    protected Move<DefenseSchedule> createUndoMove(ScoreDirector<DefenseSchedule> scoreDirector) {
        return new ChangeChiefCommissionMove(this.session, this.old_chief);
    }
    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<DefenseSchedule> scoreDirector) {
        SessionMember seat = this.session.getMembers().stream()
                .filter(sm -> sm.getRequiredRole().equals(MemberRole.CHIEF)).findFirst().get();

        scoreDirector.beforeVariableChanged(seat, "assignedMember");
        seat.setAssignedMember(this.new_chief);
        scoreDirector.afterVariableChanged(seat, "assignedMember");
    }
    @Override
    public boolean isMoveDoable(ScoreDirector<DefenseSchedule> scoreDirector) {
        return this.new_chief.getRole().equals(MemberRole.CHIEF);
    }
    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(this.session.getMembers().stream()
                .filter(sm -> sm.getRequiredRole().equals(MemberRole.CHIEF)).findFirst().get());
    }
    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(this.new_chief);
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof ChangeChiefCommissionMove other
                && session.equals(other.session)
                && new_chief.equals(other.new_chief);
    }
    @Override
    public String toString() {
        return session + "chiefs{" + old_chief + "->" + new_chief +"}";
    }
}
