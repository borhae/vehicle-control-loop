package de.joachim.haensel.phd.scenario.tasks.execution;

import java.util.List;
import java.util.stream.IntStream;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.tasks.ITask;

public class TaskExecutor
{
    public void execute(List<ITask> tasks) throws VRepException
    {
        System.out.println("about to execute the following tasks");
        IntStream.range(0, tasks.size()).forEach(idx -> System.out.format("task %d/%d: %s\n", (idx + 1), tasks.size(), tasks.get(idx).getClass().getSimpleName()));
        tasks.forEach(task -> System.out.println(task.getClass().toString()));
        int cnt = 0;
        for(ITask curTask : tasks)
        {
            curTask.execute();
            cnt++;
            System.out.format("Just finished task %d/%d\n", cnt, tasks.size());
        }
    }
}
