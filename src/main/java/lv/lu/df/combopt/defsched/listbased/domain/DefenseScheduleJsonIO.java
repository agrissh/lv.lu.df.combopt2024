package lv.lu.df.combopt.defsched.listbased.domain;

import ai.timefold.solver.jackson.impl.domain.solution.JacksonSolutionFileIO;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefenseScheduleJsonIO extends JacksonSolutionFileIO<DefenseSchedule> {
    public DefenseScheduleJsonIO() { super(DefenseSchedule.class,
            new ObjectMapper().findAndRegisterModules()); }
}
