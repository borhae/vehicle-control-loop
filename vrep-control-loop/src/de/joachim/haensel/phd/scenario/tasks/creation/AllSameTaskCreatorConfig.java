package de.joachim.haensel.phd.scenario.tasks.creation;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;

public class AllSameTaskCreatorConfig implements ITaskCreatorConfig
{
    private int _numOfTasks;
    private double _xSource;
    private double _ySource;
    private double _xTarget;
    private double _yTarget;
    private int _timeoutSec;
    
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
        _timeoutSec = ITaskCreatorConfig.estimateTimeout(xSource, ySource, xTarget, yTarget);
    }
    
    public void setAllSameOnMap(double xSource, double ySource, double xTarget, double yTarget, RoadMap map)
    {
        Position2D source = map.getClosestPointOnMap(new Position2D(xSource, ySource));
        Position2D target = map.getClosestPointOnMap(new Position2D(xTarget, yTarget));
        _xSource = source.getX();
        _ySource = source.getY();
        _xTarget = target.getX();
        _yTarget = target.getY();
        _timeoutSec = ITaskCreatorConfig.estimateTimeout(xSource, ySource, xTarget, yTarget);
    }

    @Override
    public Task getNext()
    {
        return new Task(_xSource, _ySource, _xTarget, _yTarget, _timeoutSec);
    }
}