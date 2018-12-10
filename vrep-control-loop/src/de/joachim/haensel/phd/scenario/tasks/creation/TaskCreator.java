package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.ArrayList;
import java.util.List;

public class TaskCreator
{
    private List<Task> _tasks;
    private int _numOfTasks;
    private ITaskCreatorConfig _config;

    public void configure(ITaskCreatorConfig config)
    {
        _tasks = new ArrayList<>();
        _numOfTasks = config.getNumOfTasks();
        _config = config;
    }

    public List<Task> createTasks()
    {
        for(int cnt = 0; cnt < _numOfTasks; cnt++)
        {
            _tasks.add(_config.getNext());
        }
        return _tasks;
    }
}
