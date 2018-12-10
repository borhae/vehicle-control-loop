package de.joachim.haensel.phd.scenario.tasks.execution.test;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.tasks.creation.AllSameTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.Task;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;

public class TestTaskExecutor
{
    @Test
    public void testExecuteOneDefinedAToBTask()
    {
        TaskCreator taskCreator = new TaskCreator();
        AllSameTaskCreatorConfig config = new AllSameTaskCreatorConfig(1);
        taskCreator.configure(config);
        List<Task> tasks = taskCreator.createTasks();
        
        TaskExecutor executor = new TaskExecutor();
        try
        {
            executor.setMapFileName("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            executor.execute(tasks);
        }
        catch (VRepException exc)
        {
            fail(exc.toString());
        }
    }
}
