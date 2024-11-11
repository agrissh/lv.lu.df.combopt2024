package lv.lu.df.combopt.defsched.listbased.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.Indictment;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import jakarta.annotation.PostConstruct;
import lv.lu.df.combopt.defsched.listbased.domain.DefenseSchedule;
import lv.lu.df.combopt.defsched.listbased.solver.SimpleIndictmentObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/defsched")
public class DefController {
    @Autowired
    private SolverManager<DefenseSchedule, Integer> solverManager;
    @Autowired
    private SolutionManager<DefenseSchedule, HardMediumSoftBigDecimalScore> solutionManager;

    private Map<Integer, DefenseSchedule> solutionMap = new HashMap<>();

    @PostMapping("/solve")
    public void solve(@RequestBody DefenseSchedule problem) {
        solverManager.solveAndListen(problem.getScheduleId(),
                id -> problem,
                solution -> solutionMap.put(solution.getScheduleId(), solution));
    }

    @GetMapping("/solution")
    public DefenseSchedule solution(@RequestParam Integer id) {
        return solutionMap.get(id);
    }
    @GetMapping("/list")
    public List<DefenseSchedule> list() {
        return solutionMap.values().stream().toList();
    }

    @GetMapping("/score")
    public ScoreAnalysis<HardMediumSoftBigDecimalScore> score(@RequestParam Integer id) {
        return solutionManager.analyze(solutionMap.get(id));
    }

    @GetMapping("/indictments")
    public List<SimpleIndictmentObject> indictments(@RequestParam Integer id) {
        return solutionManager.explain(solutionMap.getOrDefault(id, null)).getIndictmentMap().entrySet().stream()
                .map(entry -> {
                    Indictment<HardMediumSoftBigDecimalScore> indictment = entry.getValue();
                    return
                            new SimpleIndictmentObject(entry.getKey(), // indicted Object
                                    indictment.getScore(),
                                    indictment.getConstraintMatchCount(),
                                    indictment.getConstraintMatchSet());
                }).collect(Collectors.toList());
    }

}
