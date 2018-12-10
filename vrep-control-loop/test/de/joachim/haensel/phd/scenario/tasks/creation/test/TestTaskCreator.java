package de.joachim.haensel.phd.scenario.tasks.creation.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.creation.AllSameTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.ITaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.RandomSourceTargetTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.Task;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;

public class TestTaskCreator
{
    @Test
    public void testCreatNumberOfTasks()
    {
        TaskCreator taskCreator = new TaskCreator();
        int numOfTasks = 2;
        ITaskCreatorConfig config = new AllSameTaskCreatorConfig(numOfTasks);
        taskCreator.configure(config);
        
        List<Task> actual = taskCreator.createTasks();
        assertThat(actual, hasSize(numOfTasks));
    }

    @Test
    public void testCreateOneSpecificAtoBTask()
    {
        TaskCreator taskCreator = new TaskCreator();
        AllSameTaskCreatorConfig config = new AllSameTaskCreatorConfig(1);
        config.setAllSame(0.0, 0.0, 5.0, 0.0);
        taskCreator.configure(config);
        
        List<Task> expected = new ArrayList<>();
        Task task = new Task(0.0, 0.0, 5.0, 0.0);
        expected.add(task);
        
        List<Task> actual = taskCreator.createTasks();
        

        assertThat(actual, hasSize(1));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testCreate10RandomTasks()
    {
        TaskCreator taskCreator = new TaskCreator();
        RandomSourceTargetTaskCreatorConfig config = new RandomSourceTargetTaskCreatorConfig(10);
        XYMinMax range = new XYMinMax();
        range.update(0.0, 0.0);
        range.update(100.0, 100.0);
        config.setXYRange(range);
        taskCreator.configure(config);
        List<Task> actual = taskCreator.createTasks();
        
        assertThat(actual, hasSize(10));
        for (int idx = 0; idx < actual.size(); idx++)
        {
            Task curActualTask = actual.get(idx);
            assertThat("elem " + idx + " is null but shouldn't be", curActualTask, notNullValue());
            Position2D src = curActualTask.getSource();
            boolean srcInRange = range.isInRange(src.getX(), src.getY());
            Position2D tar = curActualTask.getTarget();
            boolean tarInRange = range.isInRange(tar.getX(), tar.getY());
            assertThat("elem " + idx + " is out of source range: " + curActualTask, srcInRange, is(true));
            assertThat("elem " + idx + " is out of target range: " + curActualTask, tarInRange, is(true));
        }
    }
}
