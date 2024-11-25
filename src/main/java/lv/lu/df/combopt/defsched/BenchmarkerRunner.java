package lv.lu.df.combopt.defsched;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.listbased.domain.DefenseScheduleJsonIO;

import java.io.File;

public class BenchmarkerRunner {
    public static void main(String[] args) {
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory
                .createFromXmlResource("BenchmarkConfigChained.xml");

        //PlannerBenchmarkFactory benchmarkFactoryFromXML = PlannerBenchmarkFactory
        //        .createFromXmlResource("BenchmarkConfig.xml");

        //PlannerBenchmarkFactory benchmarkFactoryFromFreeMarkerXML = PlannerBenchmarkFactory
        //        .createFromFreemarkerXmlResource("MatrixBenchmarkConfig.xml");

        //DefenseScheduleJsonIO io = new DefenseScheduleJsonIO();
        //DefenseSchedule problem = io.read(new File("data/example_8.json"));
        //DefenseSchedule problem = DefSchedApp.createExampleLB();

        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark();
        //PlannerBenchmark benchmark = benchmarkFactoryFromXML.buildPlannerBenchmark();
        //PlannerBenchmark benchmark = benchmarkFactoryFromFreeMarkerXML.buildPlannerBenchmark();
        benchmark.benchmarkAndShowReportInBrowser();

    }
}
