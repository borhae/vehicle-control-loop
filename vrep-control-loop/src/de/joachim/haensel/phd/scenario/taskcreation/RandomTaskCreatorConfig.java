package de.joachim.haensel.phd.scenario.taskcreation;

public class RandomTaskCreatorConfig implements ITaskCreatorConfig
{
    private int _numberOfTasks;

    public RandomTaskCreatorConfig(int numberOfTasks)
    {
        _numberOfTasks = numberOfTasks;
    }

    @Override
    public int getNumOfTasks()
    {
        return _numberOfTasks;
    }

    @Override
    public Task getNext()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
