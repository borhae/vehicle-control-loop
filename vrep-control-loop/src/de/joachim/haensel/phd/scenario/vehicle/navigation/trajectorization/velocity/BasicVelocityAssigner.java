package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryType;


/**
 * The algorithm is based on a paper by Tianyu Gu, Jarrod Snider, John M. Dolan and Jin-woo Lee
 * Focused Trajectory Planning for Autonomous On-Road Driving
 * doi:  10.1109/IVS.2013.6629524
 */
public class BasicVelocityAssigner implements IVelocityAssigner
{
    // TODO check out maximum lateral acceleration that makes sense
    // maximum lateral acceleration
    private static final double A_MAX_LATERAL = 1.0;
    // maximum longitudinal acceleration
    private double _maxSpeed;

    private double _segmentSize;
    

    public BasicVelocityAssigner(double segmentSize, double maxSpeed)
    {
        _segmentSize = segmentSize;
        _maxSpeed = maxSpeed;
    }

    @Override
    public void addVelocities(List<Trajectory> trajectories)
    {
        List<Trajectory> original = filterOutType(trajectories, TrajectoryType.OVERLAY);
        List<Trajectory> overlay = filterOutType(trajectories, TrajectoryType.ORIGINAL);
        curvatureLimitedPass(original);
        curvatureLimitedPass(overlay);
    }

    private void curvatureLimitedPass(List<Trajectory> trajectory)
    {
        for(int idx = 0; idx < trajectory.size() - 1; idx++)
        {
            Trajectory t1 = trajectory.get(idx);
            Trajectory t2 = trajectory.get(idx + 1);
            Vector2D perpendicular1 = t1.getVector().getMiddlePerpendicular();
            Vector2D perpendicular2 = t2.getVector().getMiddlePerpendicular();
            Position2D circleCenter = Vector2D.unrangedIntersect(perpendicular1, perpendicular2);
            double radius = Double.MAX_VALUE;
            if(circleCenter != null)
            {
                radius = Position2D.distance(circleCenter, t1.getVector().getBase());
            }
//            original formula where only curvature kappa is known:
//            double kappa = 1 / radius;
//            double maxCentripetal = Math.sqrt(A_MAX_LATERAL / kappa);
//            Reducible to due to curvature radius relation:
            double maxCentripetal = Math.sqrt(A_MAX_LATERAL * radius);
            double velocity = Math.min(_maxSpeed, maxCentripetal);
            t1.setSpeed(velocity);
        }
    }

    private List<Trajectory> filterOutType(List<Trajectory> trajectories, TrajectoryType type)
    {
        return trajectories.stream().filter(t -> t.hasType(type)).collect(Collectors.toList());
    }
}
