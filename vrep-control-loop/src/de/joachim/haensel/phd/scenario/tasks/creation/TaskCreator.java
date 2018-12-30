package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.tasks.ITask;

public class TaskCreator
{
    private List<ITask> _tasks;
    private ITaskCreatorConfig _config;

    public void configure(ITaskCreatorConfig config)
    {
        _tasks = new ArrayList<>();
        _config = config;
    }

    public List<ITask> createTasks()
    {
        _config.init();
        while(_config.hasNext())
        {
            _tasks.add(_config.getNext());
        }
        return _tasks;
    }
}
