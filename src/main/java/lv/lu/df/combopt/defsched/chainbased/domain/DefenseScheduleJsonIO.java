package lv.lu.df.combopt.defsched.chainbased.domain;

import ai.timefold.solver.jackson.impl.domain.solution.JacksonSolutionFileIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule;

public class DefenseScheduleJsonIO extends JacksonSolutionFileIO<lv.lu.df.combopt.defsched.chainbased.domain.DefenseSchedule> {
    public DefenseScheduleJsonIO() { super(DefenseSchedule.class,
            new ObjectMapper().findAndRegisterModules()); }
}
