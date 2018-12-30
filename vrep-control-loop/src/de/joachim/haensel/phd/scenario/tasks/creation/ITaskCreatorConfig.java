package de.joachim.haensel.phd.scenario.tasks.creation;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;

public interface ITaskCreatorConfig
{   
//    public static final double PACE_ESTIMATE = 0.06; we should be able to drive 60km/h but we aren't currently
//    public static final double PACE_ESTIMATE = 0.12; // so 30km/h must do
    public static final double PACE_ESTIMATE = 0.72; // ok lets try 5km/h


    public boolean hasNext();
    public ITask getNext();
    public void init();
    
    
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
