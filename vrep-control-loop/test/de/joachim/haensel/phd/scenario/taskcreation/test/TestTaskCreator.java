package de.joachim.haensel.phd.scenario.taskcreation.test;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

import de.joachim.haensel.phd.scenario.taskcreation.ITaskCreatorConfig;
import de.joachim.haensel.phd.scenario.taskcreation.Task;
import de.joachim.haensel.phd.scenario.taskcreation.TaskCreator;

public class TestTaskCreator
{
    public class TestTaskCreatorConfig implements ITaskCreatorConfig
    {
        private int _numOfTasks;
        private boolean _isRandomSourceAndTarget;
        
        public TestTaskCreatorConfig()
        {
            _isRandomSourceAndTarget = false;
        }

        public TestTaskCreatorConfig(int numOfTasks)
        {
            this();
            _numOfTasks = numOfTasks;
        }

        @Override
        public int getNumOfTasks()
        {
            return _numOfTasks;
        }

        @Override
        public void setRandomSourceAndTarget()
        {
            _isRandomSourceAndTarget = true;
        }
    }

    @Test
    public void testCreatNumberOfTasks()
    {
        TaskCreator taskCreator = new TaskCreator();
        ITaskCreatorConfig config = new TestTaskCreatorConfig(2);
        taskCreator.configure(config);
        
        List<Task> actual = taskCreator.createTasks();
//        org.junit.Assert.assertThat(actual, contains(new Task(), new Task()));
    }
    
    @Test
    public void testCreateOneAtoBTask()
    {
        TaskCreator taskCreator = new TaskCreator();;
        ITaskCreatorConfig config = new TestTaskCreatorConfig(1);
        config.setRandomSourceAndTarget();
        taskCreator.configure(config);
        
        List<Task> actual = taskCreator.createTasks();
        assertThat(actual, hasSize(1));
        assertThat(actual, everyItem(hasProperty("_x", is(3.3))));
        assertThat(actual, everyItem(hasProperty("_y", nullValue())));
    }
}
