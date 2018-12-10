package de.joachim.haensel.phd.scenario.tasks.creation;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class Task
{
    private double _xSource;
    private double _ySource;
    private double _xTarget;
    private double _yTarget;
    
    public Task()
    {
    }

    public Task(double xS, double yS, double xT, double yT)
    {
        _xSource = xS;
        _ySource = yS;
        _xTarget = xT;
        _yTarget = yT;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Task)
        {
            Task task = (Task)obj;
            if((task._xSource == _xSource) && (task._xTarget == _xTarget) && (task._ySource == _ySource) && (task._yTarget == _yTarget))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return String.format("source: (%f, %f), target: (%f, %f)", _xSource, _ySource, _xTarget, _yTarget);
    }

    public Position2D getSource()
    {
        return new Position2D(_xSource, _ySource);
    }

    public Position2D getTarget()
    {
        return new Position2D(_xTarget, _yTarget);
    }
}
