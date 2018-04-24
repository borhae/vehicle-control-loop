package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryType;

public class BasicVelocityAssigner implements IVelocityAssigner
{
    private double _segmentSize;
    private double _maxSpeed;

    public BasicVelocityAssigner(double segmentSize, double maxSpeed)
    {
        _segmentSize = segmentSize;
        _maxSpeed = maxSpeed;
    }

    @Override
    public void addVelocities(List<Trajectory> trajectories)
    {
//        List<Trajectory> original = trajectories.stream().filter(t -> t.isType(TrajectoryType.ORIGINAL));
        // TODO Auto-generated method stub
        for(int idx = 0; idx < trajectories.size() - 1; idx +=2)
        {
            Trajectory t1 = trajectories.get(idx);
            Trajectory t2 = trajectories.get(idx + 1);
            Vector2D perpendicular1 = t1.getVector().getMiddlePerpendicular();
            Vector2D perpendicular2 = t2.getVector().getMiddlePerpendicular();
            Position2D circleCenter = Vector2D.unrangedIntersect(perpendicular1, perpendicular2);
            double radius = Position2D.distance(circleCenter, t1.getVector().getBase());
            
        }
    }
}
