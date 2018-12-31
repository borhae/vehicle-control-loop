package de.joachim.haensel.phd.scenario.tasks.execution;

import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.tasks.ITask;

public class TaskExecutor
{
    public void execute(List<ITask> tasks) throws VRepException
    {
        for(ITask curTask : tasks)
        {
            curTask.execute();
        }
    }
}
