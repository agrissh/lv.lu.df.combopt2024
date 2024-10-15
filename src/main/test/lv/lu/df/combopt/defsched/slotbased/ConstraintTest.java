package lv.lu.df.combopt.defsched.slotbased;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import lv.lu.df.combopt.defsched.slotbased.domain.*;
import lv.lu.df.combopt.defsched.slotbased.solver.ConstraintStreamCostFunction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

public class ConstraintTest {

    public static final Session SESSION1 = new Session();
    public static final Slot SLOT1_1 = new Slot(1,
            LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0),
            LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0));

    public static final Session SESSION2 = new Session();
    public static final Slot SLOT2_1 = new Slot(1,
            LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0),
            LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0));

    public static final Person AUTHOR1 = new Person();
    public static final Person AUTHOR2 = new Person();
    public static final Person SUPERVISOR1 = new Person();
    public static final Person REVIEWER1 = new Person();

    public static final Thesis THESIS1 = new Thesis();
    public static final Thesis THESIS2 = new Thesis();
    public ConstraintTest() {
        SESSION1.getSlots().add(SLOT1_1);
        SESSION2.getSlots().add(SLOT2_1);

        THESIS1.setAuthor(AUTHOR1);
        THESIS1.setSupervisor(SUPERVISOR1);
        THESIS1.setReviewer(REVIEWER1);
        THESIS1.setThesisId(1);
        THESIS1.setDefenseSlot(SLOT1_1);

        THESIS2.setAuthor(AUTHOR2);
        THESIS2.setSupervisor(REVIEWER1);
        THESIS2.setReviewer(SUPERVISOR1);
        THESIS2.setThesisId(2);
        THESIS2.setDefenseSlot(SLOT2_1);

        AUTHOR1.getAvailableSlots().add(SLOT1_1);
        SUPERVISOR1.getAvailableSlots().add(SLOT1_1);
        REVIEWER1.getAvailableSlots().add(SLOT1_1);
    }

    ConstraintVerifier<ConstraintStreamCostFunction, DefenseSchedule> constraintVerifier = ConstraintVerifier.build(
            new ConstraintStreamCostFunction(), DefenseSchedule.class, Thesis.class);

    @Test
    void test1() {
        constraintVerifier.verifyThat(ConstraintStreamCostFunction::everyThesis)
                .given(THESIS1, THESIS2)
                .penalizesBy(2);
    }

    @Test
    void test2() {
        constraintVerifier.verifyThat(ConstraintStreamCostFunction::authorUnavailable)
                .given(THESIS1, THESIS2, AUTHOR1, AUTHOR2, SLOT1_1, SLOT2_1)
                .penalizesBy(1);
    }
}
