package lv.lu.df.combopt.defsched;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import lv.lu.df.combopt.defsched.slotbased.domain.*;
import lv.lu.df.combopt.defsched.slotbased.solver.EasyCostFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

public class DefSchedApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefSchedApp.class);
    public static void main(String[] args) {
        //System.out.println("Hello Comb Opt!");
        LOGGER.info("App started!");

        DefenseSchedule problem = createExample();
        //LOGGER.info(problem.toString());

        SolverFactory<DefenseSchedule> solverFactory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(DefenseSchedule.class)
                        .withEntityClasses(Thesis.class)
                        .withEasyScoreCalculatorClass(EasyCostFunction.class)
                        .withTerminationConfig(new TerminationConfig()
                                .withSecondsSpentLimit(5L))
                        .withEnvironmentMode(EnvironmentMode.FULL_ASSERT)
        );

        Solver<DefenseSchedule> solver = solverFactory.buildSolver();
        DefenseSchedule solution = solver.solve(problem);
        solution.printSchedule();

        LOGGER.info("App finished!");
    }

    private static DefenseSchedule createExample() {
        DefenseSchedule schedule1 = new DefenseSchedule();
        schedule1.setScheduleId(1);

        Session session1 = new Session();
        session1.setRoom("13");
        session1.setSessionId(1);
        session1.setStartingAt(LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0));
        schedule1.getSessions().add(session1);

        Slot slot1 = new Slot(1,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0));

        Slot slot2 = new Slot(2,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 0, 0));

        Slot slot3 = new Slot(3,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 30, 0));

        Slot slot4 = new Slot(4,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 11, 00, 0));

        Slot slot5 = new Slot(5,
                LocalDateTime.of(2025, Month.JANUARY, 2, 11, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 11, 30, 0));

        session1.getSlots().addAll(List.of(slot1, slot2, slot3, slot4, slot5));
        schedule1.getSlots().addAll(List.of(slot1, slot2, slot3, slot4, slot5));

        Person profAmbainis = new Person(1, "prof. Andris Ambainis", List.of(slot1, slot2, slot3));
        Person profBorzovs = new Person(2, "prof. Juris Borzovs", List.of(slot1, slot2, slot3, slot4, slot5));
        Person profSelavo = new Person(3, "prof. Leo Seļavo", List.of(slot1));
        Person docSostaks = new Person(4, "doc. Agris Šostaks", List.of(slot3, slot4, slot5));
        Person profPodnieks = new Person(5, "prof. Karlis Podnieks", List.of(slot1, slot2, slot3, slot4, slot5));

        schedule1.getPersons().addAll(List.of(profSelavo, profAmbainis, profBorzovs, profPodnieks, docSostaks));

        Person stud1 = new Person(6, "Jānis Jaunsudrabiņš", List.of(slot1, slot2, slot3, slot4, slot5));
        Person stud2 = new Person(7, "Kārlis Skalbe", List.of(slot1, slot2, slot3, slot4, slot5));
        Person stud3 = new Person(8, "Rūdolfs Blaumanis", List.of(slot1, slot2, slot3, slot4, slot5));
        Person stud4 = new Person(9, "Alberts Bels", List.of(slot1, slot2, slot3, slot4, slot5));

        schedule1.getPersons().addAll(List.of(stud1, stud2, stud3, stud4));

        Thesis thesis1 = new Thesis(1, "Baltā kvantu kriptogrāfija", null, stud1, profAmbainis, profBorzovs);
        Thesis thesis2 = new Thesis(2, "Programmatūras kvalitātes dzirnavas", null, stud2, profBorzovs, profSelavo);
        Thesis thesis3 = new Thesis(3, "Kiberfizikālās sistēmas Silmačos", null, stud3, profSelavo, docSostaks);
        Thesis thesis4 = new Thesis(4, "Datizrace būrī", null, stud4, profPodnieks, profAmbainis);

        schedule1.getThesis().addAll(List.of(thesis1, thesis2, thesis3, thesis4));
        return schedule1;
    }

}
