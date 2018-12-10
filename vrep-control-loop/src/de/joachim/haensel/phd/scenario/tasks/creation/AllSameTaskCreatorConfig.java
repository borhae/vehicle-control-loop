package de.joachim.haensel.phd.scenario.tasks.creation;

public class AllSameTaskCreatorConfig implements ITaskCreatorConfig
{
    private int _numOfTasks;
    private double _xSource;
    private double _ySource;
    private double _xTarget;
    private double _yTarget;
    
    public AllSameTaskCreatorConfig(int numOfTasks)
    {
        _numOfTasks = numOfTasks;
    }

    @Override
    public int getNumOfTasks()
    {
        return _numOfTasks;
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
        return new Task(_xSource, _ySource, _xTarget, _yTarget);
    }
}