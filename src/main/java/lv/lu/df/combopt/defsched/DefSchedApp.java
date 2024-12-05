package lv.lu.df.combopt.defsched;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import lv.lu.df.combopt.defsched.chainbased.domain.*;
import lv.lu.df.combopt.defsched.chainbased.solver.CostFunction;
import lv.lu.df.combopt.defsched.listbased.domain.ScheduleProperties;
import lv.lu.df.combopt.defsched.listbased.domain.TimeConstraint;
import lv.lu.df.combopt.defsched.slotbased.domain.*;
import lv.lu.df.combopt.defsched.slotbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.slotbased.domain.Person;
import lv.lu.df.combopt.defsched.slotbased.domain.Session;
import lv.lu.df.combopt.defsched.slotbased.domain.Thesis;
import lv.lu.df.combopt.defsched.slotbased.solver.ConstraintStreamCostFunction;
import lv.lu.df.combopt.defsched.slotbased.solver.EasyCostFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DefSchedApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefSchedApp.class);
    private static final Integer MAX_DAY_COUNT = 5;
    private static final Integer MAX_SESSION_SIZE = 8;
    private static final Integer SLOT_DURATION = 30; // minutes
    private static Integer ID = 0;
    private static Integer getID() {
        ID++;
        return ID;
    }

    private static Random random = new Random();

    private static double getRandomNumber() {
        return random.nextDouble();
    }
    public static void main(String[] args) {
        //System.out.println("Hello Comb Opt!");
        LOGGER.info("App started!");

        //runSlotBased();
        //runListBased();
        runChainBased();

        LOGGER.info("App finished!");
    }
    private static void runChainBased() {
        //lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule problem = createExampleCB();
        DefenseScheduleJsonIO io = new DefenseScheduleJsonIO();
        lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule problem = io.read(new File("data/example_real_problem.json"));

        SolverFactory<lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule> solverFactory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule.class)
                        .withEntityClasses(lv.lu.df.combopt.defsched.chainbased.domain.Thesis.class,
                                TimedEvent.class, SessionMember.class,
                                lv.lu.df.combopt.defsched.chainbased.domain.Session.class)
                        .withConstraintProviderClass(CostFunction.class)
                        .withTerminationConfig(new TerminationConfig()
                                .withSecondsSpentLimit(200L))
                        .withEnvironmentMode(EnvironmentMode.REPRODUCIBLE)
        );

        Solver<lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule> solver = solverFactory.buildSolver();

        lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule solution = solver.solve(problem);
        solution.printSchedule();

        SolutionManager<lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule, HardMediumSoftBigDecimalScore> solutionManager = SolutionManager.create(solverFactory);

        LOGGER.debug(solutionManager.explain(solution).getSummary());
        //printScoreSummary(solutionManager.explain(solution));
        //ScoreAnalysis<HardSoftScore> scoreAnalysis = solutionManager.analyze(solution);
        //LOGGER.debug(scoreAnalysis.summarize());
    }
    private static void runListBased() {
        //lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule problem = createExampleLB();
        lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule problem = generateExampleLB(150);
        lv.lu.df.combopt.defsched.listbased.domain.DefenseScheduleJsonIO io =
                new lv.lu.df.combopt.defsched.listbased.domain.DefenseScheduleJsonIO();
        io.write(problem, new File("data/example_150.json"));
        lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule problem2 =
                io.read(new File("data/example_150.json"));
        SolverFactory<lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule> solverFactoryFromConfigXML =
            SolverFactory.createFromXmlResource("SolverConfig.xml");

        SolverFactory<lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule> solverFactory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule.class)
                        .withEntityClasses(lv.lu.df.combopt.defsched.listbased.domain.Thesis.class,
                                lv.lu.df.combopt.defsched.listbased.domain.Session.class)
                        .withConstraintProviderClass(lv.lu.df.combopt.defsched.listbased.solver.ConstraintStreamCostFunction.class)
                        .withTerminationConfig(new TerminationConfig()
                                .withSecondsSpentLimit(5L))
                        .withEnvironmentMode(EnvironmentMode.FULL_ASSERT)
        );

        //Solver<lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule> solver = solverFactory.buildSolver();
        Solver<lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule> solver = solverFactoryFromConfigXML.buildSolver();
        lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule solution =
                solver.solve(problem2);
        solution.printSchedule();
        //io.write(solution, new File("data/example_8_solution.json"));

        SolutionManager<lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule, HardMediumSoftBigDecimalScore> solutionManager = SolutionManager.create(solverFactory);
        //SolutionManager<lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule, HardMediumSoftBigDecimalScore> solutionManager = SolutionManager.create(solverFactoryFromConfigXML);
        LOGGER.debug(solutionManager.explain(solution).getSummary());
        //printScoreSummary(solutionManager.explain(solution));
        //ScoreAnalysis<HardSoftScore> scoreAnalysis = solutionManager.analyze(solution);
        //LOGGER.debug(scoreAnalysis.summarize());
    }
    private static void runSlotBased() {
        DefenseSchedule problem = createExample();
        //LOGGER.info(problem.toString());

        SolverFactory<DefenseSchedule> solverFactory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(DefenseSchedule.class)
                        .withEntityClasses(Thesis.class)
                        //.withEasyScoreCalculatorClass(EasyCostFunction.class)
                        .withConstraintProviderClass(ConstraintStreamCostFunction.class)
                        .withTerminationConfig(new TerminationConfig()
                                .withSecondsSpentLimit(5L))
                        .withEnvironmentMode(EnvironmentMode.FULL_ASSERT)
        );

        Solver<DefenseSchedule> solver = solverFactory.buildSolver();
        DefenseSchedule solution = solver.solve(problem);
        solution.printSchedule();

        SolutionManager<DefenseSchedule, HardSoftScore> solutionManager = SolutionManager.create(solverFactory);
        LOGGER.debug(solutionManager.explain(solution).getSummary());
        //printScoreSummary(solutionManager.explain(solution));
        //ScoreAnalysis<HardSoftScore> scoreAnalysis = solutionManager.analyze(solution);
        //LOGGER.debug(scoreAnalysis.summarize());
    }

    private static void printScoreSummary(ScoreExplanation<DefenseSchedule, HardSoftScore> scoreExplanation) {
        scoreExplanation.getIndictmentMap().forEach((o, ind) -> {
            LOGGER.info(o.toString() + " : " + ind.getScore() + " (" + ind.getConstraintMatchCount()+ ")");
            ind.getConstraintMatchSet().forEach(cm -> {
                LOGGER.info("     " + cm.getScore() + " " + cm.getIdentificationString());
            });
        });

        scoreExplanation.getConstraintMatchTotalMap().forEach((s, cmTotal) -> {
            LOGGER.info(s + " : " + cmTotal.getScore() + " (" + cmTotal.getConstraintMatchCount() + ")");
            cmTotal.getConstraintMatchSet().forEach(cm -> {
                LOGGER.info("     " + cm.getScore() + " " + cm.getIdentificationString());
            });
        });
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

        Session session2 = new Session();
        session2.setRoom("16");
        session2.setSessionId(2);
        session2.setStartingAt(LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0));
        schedule1.getSessions().add(session2);

        Slot slot21 = new Slot(21,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0));

        Slot slot22 = new Slot(22,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 0, 0));

        Slot slot23 = new Slot(23,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 30, 0));

        Slot slot24 = new Slot(24,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 11, 00, 0));

        Slot slot25 = new Slot(25,
                LocalDateTime.of(2025, Month.JANUARY, 2, 11, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 11, 30, 0));

        session1.getSlots().addAll(List.of(slot1, slot2, slot3, slot4, slot5));
        schedule1.getSlots().addAll(List.of(slot1, slot2, slot3, slot4, slot5));
        session2.getSlots().addAll(List.of(slot21, slot22, slot23, slot24, slot25));
        schedule1.getSlots().addAll(List.of(slot21, slot22, slot23, slot24, slot25));

        Person profAmbainis = new Person(1, "prof. Andris Ambainis", List.of(slot1, slot2, slot3, slot21, slot22, slot23));
        Person profBorzovs = new Person(2, "prof. Juris Borzovs", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person profSelavo = new Person(3, "prof. Leo Seļavo", List.of(slot1, slot21));
        Person docSostaks = new Person(4, "doc. Agris Šostaks", List.of(slot3, slot4, slot5, slot23, slot24, slot25));
        Person profPodnieks = new Person(5, "prof. Karlis Podnieks", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person profNiedrite = new Person(6, "prof. Laila Niedrīte", List.of(slot1,  slot3, slot4, slot5, slot21, slot23, slot24, slot25));
        Person profBarzdins = new Person(7, "prof. Guntis Bārzdiņš", List.of(slot1, slot2, slot21, slot22));

        schedule1.getPersons().addAll(List.of(profSelavo, profAmbainis, profBorzovs, profPodnieks,
                docSostaks, profNiedrite, profBarzdins));

        Person stud1 = new Person(6, "Jānis Jaunsudrabiņš", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person stud2 = new Person(7, "Kārlis Skalbe", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person stud3 = new Person(8, "Rūdolfs Blaumanis", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person stud4 = new Person(9, "Alberts Bels", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person stud5 = new Person(10, "Vilis Plūdons", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person stud6 = new Person(11, "Andrejs Pumpurs", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person stud7 = new Person(12, "Aleksandrs Čaks", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));
        Person stud8 = new Person(13, "Vizma Belševica", List.of(slot1, slot2, slot3, slot4, slot5, slot21, slot22, slot23, slot24, slot25));

        schedule1.getPersons().addAll(List.of(stud1, stud2, stud3, stud4, stud5, stud6, stud7, stud8));

        Thesis thesis1 = new Thesis(1, "Baltā kvantu kriptogrāfija", null, stud1, profAmbainis, profBorzovs);
        Thesis thesis2 = new Thesis(2, "Programmatūras kvalitātes dzirnavas", null, stud2, profBorzovs, profSelavo);
        Thesis thesis3 = new Thesis(3, "Kiberfizikālās sistēmas Silmačos", null, stud3, profSelavo, docSostaks);
        Thesis thesis4 = new Thesis(4, "Datizrace būrī", null, stud4, profPodnieks, profAmbainis);
        Thesis thesis5 = new Thesis(5, "Mazā Anduļa datu noliktavas", null, stud5, profNiedrite, profPodnieks);
        Thesis thesis6 = new Thesis(6, "Tīklplēsis", null, stud6, profBarzdins, profSelavo);
        Thesis thesis7 = new Thesis(7, "Neironu skartie", null, stud7, profBarzdins, profAmbainis);
        Thesis thesis8 = new Thesis(8, "Billes datu noliktavas", null, stud8, profNiedrite, docSostaks);

        schedule1.getThesis().addAll(List.of(thesis1, thesis2, thesis3, thesis4, thesis5, thesis6, thesis7, thesis8));
        return schedule1;
    }

    private static lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule generateExampleLB(Integer N) {
        // N - number of thesis. We want to split works in sessions with no more than MAX_SESSION_SIZE theses.
        // N div MAX_DAY_COUNT ---> thesis every day ... if
        int DAILY_N = N / MAX_DAY_COUNT + 1;
        int DAILY_SESSION_N = DAILY_N / MAX_SESSION_SIZE + 1;
        List<LocalDateTime> DAYS = List.of(
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 3, 9, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 4, 9, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 5, 9, 0, 0),
                LocalDateTime.of(2025, Month.JANUARY, 6, 9, 0, 0));
        List<String> ROOMS = List.of("13", "16", "18", "12", "14");
        lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule problem = new lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule();
        problem.setScheduleId(getID());

        ScheduleProperties props = new ScheduleProperties();
        problem.setProperties(props);
        props.setSessionSize(MAX_SESSION_SIZE);


        for (LocalDateTime day : DAYS) {
            for (int i = 1; i<=DAILY_SESSION_N; i++) {
                lv.lu.df.combopt.defsched.listbased.domain.Session session =
                        new lv.lu.df.combopt.defsched.listbased.domain.Session();
                session.setRoom(ROOMS.get(i));
                session.setSessionId(getID());
                session.setStartingAt(day);
                session.setSlotDurationMinutes(SLOT_DURATION);
                problem.getSessions().add(session);
            }
        }

        List<lv.lu.df.combopt.defsched.listbased.domain.Person> profs = new ArrayList<>();
        for (int i=1; i<=N / 3;i++) {
            lv.lu.df.combopt.defsched.listbased.domain.Person prof =
                    new lv.lu.df.combopt.defsched.listbased.domain.Person(getID(), "P" + i, new ArrayList<>());
            if (getRandomNumber() > 0.7) {
                LocalDateTime tc_from = DAYS.get(random.nextInt(DAYS.size()));
                TimeConstraint tc = new TimeConstraint(getID(),
                        tc_from,
                        tc_from.plusHours(random.nextInt(6, 48)));
                prof.getTimeConstraints().add(tc);
            }
            problem.getPersons().add(prof);
            profs.add(prof);
        }


        for (int i = 1; i<=N; i++) {
            // Student
            lv.lu.df.combopt.defsched.listbased.domain.Person stud =
                    new lv.lu.df.combopt.defsched.listbased.domain.Person(getID(), "S" + i, List.of());
            problem.getPersons().add(stud);

            lv.lu.df.combopt.defsched.listbased.domain.Person sup = profs.get(random.nextInt(profs.size()));
            lv.lu.df.combopt.defsched.listbased.domain.Person rev = profs.get(random.nextInt(profs.size()));
            while (sup.equals(rev)) {
                rev = profs.get(random.nextInt(profs.size()));
            }
            // Thesis
            lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis =
                    new lv.lu.df.combopt.defsched.listbased.domain.Thesis(getID(), "T" + i, stud,
                            sup,
                            rev,
                            null, null, null, null, null);

            problem.getThesis().add(thesis);
        }
        return problem;
    }

    public static lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule createExampleLB() {
        lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule schedule1 = new lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule();
        schedule1.setScheduleId(1);

        lv.lu.df.combopt.defsched.listbased.domain.Session session1 = new lv.lu.df.combopt.defsched.listbased.domain.Session();
        session1.setRoom("13");
        session1.setSessionId(1);
        session1.setStartingAt(LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0));
        session1.setSlotDurationMinutes(30);
        schedule1.getSessions().add(session1);

        lv.lu.df.combopt.defsched.listbased.domain.Session session2 = new lv.lu.df.combopt.defsched.listbased.domain.Session();
        session2.setRoom("16");
        session2.setSessionId(2);
        session2.setStartingAt(LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0));
        session2.setSlotDurationMinutes(30);
        schedule1.getSessions().add(session2);

        TimeConstraint tcProfAmbainis = new TimeConstraint(1,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 45, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 23, 59, 59));
        lv.lu.df.combopt.defsched.listbased.domain.Person profAmbainis =
                new lv.lu.df.combopt.defsched.listbased.domain.Person(1, "prof. Andris Ambainis", List.of(tcProfAmbainis));
        lv.lu.df.combopt.defsched.listbased.domain.Person profBorzovs =
                new lv.lu.df.combopt.defsched.listbased.domain.Person(2, "prof. Juris Borzovs", List.of());
        TimeConstraint tcProfSelavo = new TimeConstraint(2,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 23, 59, 59));
        lv.lu.df.combopt.defsched.listbased.domain.Person profSelavo =
                new lv.lu.df.combopt.defsched.listbased.domain.Person(3, "prof. Leo Seļavo", List.of(tcProfSelavo));
        TimeConstraint tcdocSostaks = new TimeConstraint(3,
                LocalDateTime.of(2025, Month.JANUARY, 2, 8, 45, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 59, 59));
        lv.lu.df.combopt.defsched.listbased.domain.Person docSostaks =
                new lv.lu.df.combopt.defsched.listbased.domain.Person(4, "doc. Agris Šostaks", List.of(tcdocSostaks));
        lv.lu.df.combopt.defsched.listbased.domain.Person profPodnieks =
                new lv.lu.df.combopt.defsched.listbased.domain.Person(5, "prof. Karlis Podnieks", List.of());
        TimeConstraint tcProfNiedrite = new TimeConstraint(4,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 0, 0));
        lv.lu.df.combopt.defsched.listbased.domain.Person profNiedrite =
                new lv.lu.df.combopt.defsched.listbased.domain.Person(6, "prof. Laila Niedrīte", List.of(tcProfNiedrite));
        TimeConstraint tcProfBarzdins = new TimeConstraint(5,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 15, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 23, 59, 59));
        lv.lu.df.combopt.defsched.listbased.domain.Person profBarzdins =
                new lv.lu.df.combopt.defsched.listbased.domain.Person(7, "prof. Guntis Bārzdiņš", List.of(tcProfBarzdins));

        schedule1.getPersons().addAll(List.of(profSelavo, profAmbainis, profBorzovs, profPodnieks,
                docSostaks, profNiedrite, profBarzdins));

        lv.lu.df.combopt.defsched.listbased.domain.Person stud1 = new lv.lu.df.combopt.defsched.listbased.domain.Person(6, "Jānis Jaunsudrabiņš", List.of());
        lv.lu.df.combopt.defsched.listbased.domain.Person stud2 = new lv.lu.df.combopt.defsched.listbased.domain.Person(7, "Kārlis Skalbe", List.of());
        lv.lu.df.combopt.defsched.listbased.domain.Person stud3 = new lv.lu.df.combopt.defsched.listbased.domain.Person(8, "Rūdolfs Blaumanis", List.of());
        lv.lu.df.combopt.defsched.listbased.domain.Person stud4 = new lv.lu.df.combopt.defsched.listbased.domain.Person(9, "Alberts Bels", List.of());
        lv.lu.df.combopt.defsched.listbased.domain.Person stud5 = new lv.lu.df.combopt.defsched.listbased.domain.Person(10, "Vilis Plūdons", List.of());
        lv.lu.df.combopt.defsched.listbased.domain.Person stud6 = new lv.lu.df.combopt.defsched.listbased.domain.Person(11, "Andrejs Pumpurs", List.of());
        lv.lu.df.combopt.defsched.listbased.domain.Person stud7 = new lv.lu.df.combopt.defsched.listbased.domain.Person(12, "Aleksandrs Čaks", List.of());
        lv.lu.df.combopt.defsched.listbased.domain.Person stud8 = new lv.lu.df.combopt.defsched.listbased.domain.Person(13, "Vizma Belševica", List.of());

        schedule1.getPersons().addAll(List.of(stud1, stud2, stud3, stud4, stud5, stud6, stud7, stud8));

        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis1 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(1, "Baltā kvantu kriptogrāfija", stud1, profAmbainis, profBorzovs, null, null, null, null, null);
        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis2 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(2, "Programmatūras kvalitātes dzirnavas", stud2, profBorzovs, profSelavo, null, null, null, null, null);
        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis3 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(3, "Kiberfizikālās sistēmas Silmačos", stud3, profSelavo, docSostaks, null, null, null, null, null);
        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis4 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(4, "Datizrace būrī", stud4, profPodnieks, profAmbainis, null, null, null, null, null);
        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis5 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(5, "Mazā Anduļa datu noliktavas", stud5, profNiedrite, profPodnieks, null, null, null, null, null);
        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis6 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(6, "Tīklplēsis", stud6, profBarzdins, profSelavo, null, null, null, null, null);
        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis7 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(7, "Neironu skartie", stud7, profBarzdins, profAmbainis, null, null, null, null, null);
        lv.lu.df.combopt.defsched.listbased.domain.Thesis thesis8 = new lv.lu.df.combopt.defsched.listbased.domain.Thesis(8, "Billes datu noliktavas", stud8, profNiedrite, docSostaks, null, null, null, null, null);

        schedule1.getThesis().addAll(List.of(thesis1, thesis2, thesis3, thesis4, thesis5, thesis6, thesis7, thesis8));
        return schedule1;
    }

    private static lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule createExampleCB() {
        lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule schedule1 = new lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule();
        schedule1.setScheduleId(1);

        Program program = new Program("MASTER");
        schedule1.getPrograms().add(program);

        lv.lu.df.combopt.defsched.chainbased.domain.Session session1 = new lv.lu.df.combopt.defsched.chainbased.domain.Session();
        session1.setRoom("13");
        session1.setSessionId(1);
        session1.setSessionStart(LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0));
        session1.setSlotDurationMinutes(30);

        SessionMember sm11 = new SessionMember(1, null, MemberRole.CHIEF);
        SessionMember sm12 = new SessionMember(2, null, MemberRole.SECRETARY);
        SessionMember sm13 = new SessionMember(3, null, null);
        session1.getMembers().addAll(List.of(sm12,sm13,sm11));

        schedule1.getSessionMembers().addAll(List.of(sm12,sm13,sm11));
        schedule1.getSessions().add(session1);


        lv.lu.df.combopt.defsched.chainbased.domain.Session session2 = new lv.lu.df.combopt.defsched.chainbased.domain.Session();
        session2.setRoom("16");
        session2.setSessionId(2);
        session2.setSessionStart(LocalDateTime.of(2025, Month.JANUARY, 2, 9, 0, 0));
        session2.setSlotDurationMinutes(30);

        SessionMember sm21 = new SessionMember(4, null, MemberRole.CHIEF);
        SessionMember sm22 = new SessionMember(5, null, MemberRole.SECRETARY);
        SessionMember sm23 = new SessionMember(6, null, null);
        session2.getMembers().addAll(List.of(sm23,sm22,sm21));

        schedule1.getSessionMembers().addAll(List.of(sm23,sm22,sm21));
        schedule1.getSessions().add(session2);

        lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint tcProfAmbainis =
                new lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint(1,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 45, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 23, 59, 59));
        Member mbProfAmbainis = new Member(1, MemberRole.MEMBER, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person profAmbainis =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(1, "prof. Andris Ambainis", List.of(tcProfAmbainis),
                        List.of(mbProfAmbainis));
        Member mbProfBorzovs = new Member(2, MemberRole.CHIEF, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person profBorzovs =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(2, "prof. Juris Borzovs", List.of(),
                        List.of(mbProfBorzovs));
        lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint tcProfSelavo =
                new lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint(2,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 23, 59, 59));
        Member mbProfSelavo = new Member(3, MemberRole.MEMBER, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person profSelavo =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(3, "prof. Leo Seļavo", List.of(tcProfSelavo),
                        List.of(mbProfSelavo));
        lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint tcdocSostaks =
                new lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint(3,
                LocalDateTime.of(2025, Month.JANUARY, 2, 8, 45, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 59, 59));
        Member mbDocSostaks = new Member(4, MemberRole.SECRETARY, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person docSostaks =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(4, "doc. Agris Šostaks", List.of(),
                        List.of(mbDocSostaks));
        Member mbProfPodnieks = new Member(5, MemberRole.CHIEF, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person profPodnieks =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(5, "prof. Karlis Podnieks", List.of(),
                        List.of(mbProfPodnieks));
        lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint tcProfNiedrite =
                new lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint(4,
                LocalDateTime.of(2025, Month.JANUARY, 2, 9, 30, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 0, 0));
        Member mbProfNiedrite = new Member(6, MemberRole.MEMBER, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person profNiedrite =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(6, "prof. Laila Niedrīte", List.of(tcProfNiedrite),
                        List.of(mbProfNiedrite));
        lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint tcProfBarzdins =
                new lv.lu.df.combopt.defsched.chainbased.domain.TimeConstraint(5,
                LocalDateTime.of(2025, Month.JANUARY, 2, 10, 15, 0),
                LocalDateTime.of(2025, Month.JANUARY, 2, 23, 59, 59));
        Member mbProfBarzdins = new Member(7, MemberRole.MEMBER, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person profBarzdins =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(7, "prof. Guntis Bārzdiņš", List.of(tcProfBarzdins),
                        List.of(mbProfBarzdins));
        Member mbDocRencis = new Member(8, MemberRole.SECRETARY, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person docRencis =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(101, "doc. Edgars Rencis", List.of(),
                        List.of(mbDocRencis));
        Member mbProfCelms = new Member(9, MemberRole.CHIEF, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person profCelms =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(102, "asoc. prof. Edgars Celms", List.of(),
                        List.of(mbProfCelms));
        Member mbDocFreivalds = new Member(10, MemberRole.MEMBER, false, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Person docFreivalds =
                new lv.lu.df.combopt.defsched.chainbased.domain.Person(103, "doc. Karlis Freivalds", List.of(),
                        List.of(mbDocFreivalds));
        schedule1.getPersons().addAll(List.of(profSelavo, profAmbainis, profBorzovs, profPodnieks,
                docSostaks, profNiedrite, profBarzdins,docRencis,profCelms,docFreivalds));
        schedule1.getMembers().addAll(List.of(mbProfAmbainis,mbProfNiedrite,mbProfBarzdins,mbProfPodnieks,
                mbProfBorzovs,mbDocSostaks,mbProfSelavo,mbDocRencis,mbProfCelms,mbDocFreivalds));

        lv.lu.df.combopt.defsched.chainbased.domain.Person stud1 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(6, "Jānis Jaunsudrabiņš", List.of(), List.of());
        lv.lu.df.combopt.defsched.chainbased.domain.Person stud2 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(7, "Kārlis Skalbe", List.of(), List.of());
        lv.lu.df.combopt.defsched.chainbased.domain.Person stud3 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(8, "Rūdolfs Blaumanis", List.of(), List.of());
        lv.lu.df.combopt.defsched.chainbased.domain.Person stud4 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(9, "Alberts Bels", List.of(), List.of());
        lv.lu.df.combopt.defsched.chainbased.domain.Person stud5 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(10, "Vilis Plūdons", List.of(), List.of());
        lv.lu.df.combopt.defsched.chainbased.domain.Person stud6 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(11, "Andrejs Pumpurs", List.of(), List.of());
        lv.lu.df.combopt.defsched.chainbased.domain.Person stud7 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(12, "Aleksandrs Čaks", List.of(), List.of());
        lv.lu.df.combopt.defsched.chainbased.domain.Person stud8 = new lv.lu.df.combopt.defsched.chainbased.domain.Person(13, "Vizma Belševica", List.of(), List.of());

        schedule1.getPersons().addAll(List.of(stud1, stud2, stud3, stud4, stud5, stud6, stud7, stud8));

        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis1 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(1, "Baltā kvantu kriptogrāfija", stud1, profAmbainis, profBorzovs, null, null, null, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis2 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(2, "Programmatūras kvalitātes dzirnavas", stud2, profBorzovs, profSelavo, null, null, null, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis3 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(3, "Kiberfizikālās sistēmas Silmačos", stud3, profSelavo, docSostaks, null, null, null, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis4 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(4, "Datizrace būrī", stud4, profPodnieks, profAmbainis, null, null, null, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis5 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(5, "Mazā Anduļa datu noliktavas", stud5, profNiedrite, profPodnieks, null, null, null, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis6 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(6, "Tīklplēsis", stud6, profBarzdins, profSelavo, null, null, null, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis7 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(7, "Neironu skartie", stud7, profBarzdins, profAmbainis, null, null, null, program);
        lv.lu.df.combopt.defsched.chainbased.domain.Thesis thesis8 = new lv.lu.df.combopt.defsched.chainbased.domain.Thesis(8, "Billes datu noliktavas", stud8, profNiedrite, docSostaks, null, null, null, program);

        schedule1.getThesis().addAll(List.of(thesis1, thesis2, thesis3, thesis4, thesis5, thesis6, thesis7, thesis8));
        return schedule1;
    }

}
