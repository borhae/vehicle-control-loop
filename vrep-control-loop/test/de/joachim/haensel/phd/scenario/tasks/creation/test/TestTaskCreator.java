package de.joachim.haensel.phd.scenario.tasks.creation.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.DriveAtoBTask;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.AllSameAToBDrivingTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.RandomSourceTargetTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;

public class TestTaskCreator
{
    @Test
    public void testCreatNumberOfTasks()
    {
        TaskCreator taskCreator = new TaskCreator();
        int numOfTasks = 2;
        AllSameAToBDrivingTaskCreatorConfig config = new AllSameAToBDrivingTaskCreatorConfig(numOfTasks, true);
        RoadMap map = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        config.setMap(map);
        taskCreator.configure(config);
        
        List<ITask> tasks = taskCreator.createTasks();
        List<ITask> actual = tasks.stream().filter(task -> task instanceof DriveAtoBTask).collect(Collectors.toList());
        assertThat(actual, hasSize(numOfTasks));
    }

    @Test
    public void testCreateOneSpecificAtoBTask()
    {
        TaskCreator taskCreator = new TaskCreator();
        AllSameAToBDrivingTaskCreatorConfig config = new AllSameAToBDrivingTaskCreatorConfig(1, true);
        RoadMap map = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        config.setMap(map);
        config.setAllSame(0.0, 0.0, 5.0, 0.0);
        taskCreator.configure(config);
        
        List<DriveAtoBTask> expected = new ArrayList<>();
        DriveAtoBTask task = new DriveAtoBTask(0.0, 0.0, 5.0, 0.0, 0, null, null);
        expected.add(task);
        
        List<ITask> tasks = taskCreator.createTasks();
        List<ITask> actual = tasks.stream().filter(curTask -> curTask instanceof DriveAtoBTask).collect(Collectors.toList());

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
        List<ITask> actual = taskCreator.createTasks();
        
        assertThat(actual, hasSize(10));
        for (int idx = 0; idx < actual.size(); idx++)
        {
            ITask curActualTask = actual.get(idx);
            assertThat("elem " + idx + " is null but shouldn't be", curActualTask, notNullValue());
            if(curActualTask instanceof DriveAtoBTask)
            {
                Position2D src = ((DriveAtoBTask)curActualTask).getSource();
                boolean srcInRange = range.isInRange(src.getX(), src.getY());
                Position2D tar = ((DriveAtoBTask)curActualTask).getTarget();
                boolean tarInRange = range.isInRange(tar.getX(), tar.getY());
                assertThat("elem " + idx + " is out of source range: " + curActualTask, srcInRange, is(true));
                assertThat("elem " + idx + " is out of target range: " + curActualTask, tarInRange, is(true));
            }
        }
    }
}
