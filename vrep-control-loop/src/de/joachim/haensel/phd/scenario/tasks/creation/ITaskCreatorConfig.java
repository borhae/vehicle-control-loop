package de.joachim.haensel.phd.scenario.tasks.creation;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public interface ITaskCreatorConfig
{   
    public static final double PACE_ESTIMATE = 0.06;

    public int getNumOfTasks();

    public Task getNext();
    
    public static int estimateTimeout(double xSource, double ySource, double xTarget, double yTarget)
    {
        //things are measured in Meter. 
        return (int)((new Position2D(xSource, ySource)).distance(new Position2D(xTarget, yTarget)) * PACE_ESTIMATE);
    }

    public static int estimateTimeout(Position2D source, Position2D target)
    {
        return estimateTimeout(source.getX(), source.getY(), target.getY(), target.getY());
    }
}
