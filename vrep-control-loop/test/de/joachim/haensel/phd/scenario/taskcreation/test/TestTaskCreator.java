package de.joachim.haensel.phd.scenario.taskcreation.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.taskcreation.ITaskCreatorConfig;
import de.joachim.haensel.phd.scenario.taskcreation.RandomTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.taskcreation.Task;
import de.joachim.haensel.phd.scenario.taskcreation.TaskCreator;

public class TestTaskCreator
{
    public class TestTaskCreatorConfig implements ITaskCreatorConfig
    {
        private int _numOfTasks;
        private boolean _isRandomSourceAndTarget;
        private double _xSource;
        private double _ySource;
        private double _xTarget;
        private double _yTarget;
        
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

        public void setRandomSourceAndTarget()
        {
            _isRandomSourceAndTarget = true;
        }

        public void setAllSame(double xSource, double ySource, double xTarget, double yTarget)
        {
            _xSource = xSource;
            _ySource = ySource;
            _xTarget = xTarget;
            _yTarget = yTarget;
        }

        @Override
        public Task getNext()
        {
            if(_isRandomSourceAndTarget)
            {
                return new Task();
            }
            else
            {
                return new Task(_xSource, _ySource, _xTarget, _yTarget);
            }
        }
    }

    @Test
    public void testCreatNumberOfTasks()
    {
        TaskCreator taskCreator = new TaskCreator();
        int numOfTasks = 2;
        ITaskCreatorConfig config = new TestTaskCreatorConfig(numOfTasks);
        taskCreator.configure(config);
        
        List<Task> actual = taskCreator.createTasks();
        assertThat(actual, hasSize(numOfTasks));
    }

    @Test
    public void testCreateOneSpecificAtoBTask()
    {
        TaskCreator taskCreator = new TaskCreator();
        TestTaskCreatorConfig config = new TestTaskCreatorConfig(1);
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
        RandomTaskCreatorConfig config = new RandomTaskCreatorConfig(10);
        config.setXYRange();
        taskCreator.configure(config);
        
        
    }
}
