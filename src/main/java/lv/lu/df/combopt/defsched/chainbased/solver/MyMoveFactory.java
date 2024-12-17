package lv.lu.df.combopt.defsched.chainbased.solver;

import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.move.factory.MoveListFactory;
import lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.chainbased.domain.MemberRole;

import java.util.ArrayList;
import java.util.List;

public class MyMoveFactory implements MoveListFactory<DefenseSchedule> {
    @Override
    public List<? extends Move<DefenseSchedule>> createMoveList(DefenseSchedule defenseSchedule) {
        List<ChangeChiefCommissionMove> moveList = new ArrayList<>();
        defenseSchedule.getSessions().forEach(
                sess -> {
                    defenseSchedule.getMembers().stream()
                            .filter(m -> m.getRole().equals(MemberRole.CHIEF))
                            .forEach( chief -> moveList.add(new ChangeChiefCommissionMove(sess, chief)));
                }
        );
        return moveList;
    }
}
