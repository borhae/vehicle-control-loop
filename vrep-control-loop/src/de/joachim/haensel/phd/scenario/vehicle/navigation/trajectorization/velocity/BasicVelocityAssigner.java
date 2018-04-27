package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory.VelocityEdgeType;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryType;


/**
 * The algorithm is based on a paper by Tianyu Gu, Jarrod Snider, John M. Dolan and Jin-woo Lee
 * Focused Trajectory Planning for Autonomous On-Road Driving
 * doi:  10.1109/IVS.2013.6629524
 */
public class BasicVelocityAssigner implements IVelocityAssigner
{
    public class SpeedRegion
    {

    }

    // TODO check out maximum lateral acceleration that makes sense
    // maximum lateral acceleration
    private static final double A_MAX_LATERAL = 3.0;
    // maximum longitudinal acceleration -> throttle
    private static final double A_MAX_LONGITUDINAL = 4.0;
    // maximum longitudinal deceleration -> brake
    private static final double D_MAX_LONGITUDINAL = 4.0;
    // maximum longitudinal velocity
    private double _maxSpeed;
    private double _segmentSize;

    public BasicVelocityAssigner(double segmentSize, double maxSpeed)
    {
        _maxSpeed = maxSpeed;
        _segmentSize = segmentSize;
    }

    @Override
    public void addVelocities(List<Trajectory> trajectories)
    {
        List<Trajectory> original = filterOutType(trajectories, TrajectoryType.OVERLAY);
        List<Trajectory> overlay = filterOutType(trajectories, TrajectoryType.ORIGINAL);
        curvatureLimitedPass(original);
        curvatureLimitedPass(overlay);
        identifyRegions(trajectories);
        fillInitialPadding(trajectories);
        capAccelerationDeceleration(trajectories);
    }

    private void capAccelerationDeceleration(List<Trajectory> trajectories)
    {
        for (int idx = 0; idx < trajectories.size(); idx++)
        {
            if(idx == trajectories.size() - 1)
            {
                continue;
            }
            Trajectory curTrajectory = trajectories.get(idx);
            Trajectory nextTrajectory = trajectories.get(idx + 1); 
            double v_i_sq = curTrajectory.getVelocity() * curTrajectory.getVelocity();
            double v_i_p1_sq = nextTrajectory.getVelocity() * nextTrajectory.getVelocity();
            if(curTrajectory.getRiseFall() == VelocityEdgeType.RAISE)
            {
                double acc = (v_i_p1_sq - v_i_sq) / (2 * _segmentSize);
                if(acc >= A_MAX_LONGITUDINAL)
                {
                    double newVelocity = Math.sqrt(v_i_sq + 2*_segmentSize*A_MAX_LONGITUDINAL);
                    nextTrajectory.setVelocity(newVelocity);
                }
            }
            if(curTrajectory.getRiseFall() == VelocityEdgeType.FALL)
            {
                double dec = (v_i_sq - v_i_p1_sq) / (2 * _segmentSize);
                if(dec >= D_MAX_LONGITUDINAL)
                {
                    double newVelocity = Math.sqrt(v_i_p1_sq + 2*_segmentSize*D_MAX_LONGITUDINAL);
                    curTrajectory.setVelocity(newVelocity);
                }
            }
        }
    }

    private void fillInitialPadding(List<Trajectory> trajectories)
    {
        Trajectory nextTrajectory = null;
        for (int idx = trajectories.size() - 1; idx >= 0; idx--)
        {
            Trajectory curTrajectory = trajectories.get(idx);
            if((nextTrajectory != null) && (curTrajectory.getRiseFall() == VelocityEdgeType.START))
            {
                curTrajectory.setRiseFall(nextTrajectory.getRiseFall());
            }
            nextTrajectory = curTrajectory;
        }
    }

    private void identifyRegions(List<Trajectory> trajectories)
    {
        Trajectory lastTrajectory = null; 
        for (Trajectory curTrajectory : trajectories)
        {
            if(lastTrajectory == null)
            {
                curTrajectory.setRiseFall(VelocityEdgeType.START);
            }
            else if(curTrajectory.getVelocity() > lastTrajectory.getVelocity())
            {
                curTrajectory.setRiseFall(VelocityEdgeType.RAISE);
            }
            else if(curTrajectory.getVelocity() < lastTrajectory.getVelocity())
            {
                curTrajectory.setRiseFall(VelocityEdgeType.FALL);
            }
            else
            {
                curTrajectory.setRiseFall(lastTrajectory.getRiseFall());
            }
            lastTrajectory = curTrajectory;
        }
    }

    private void curvatureLimitedPass(List<Trajectory> trajectory)
    {
        for(int idx = 0; idx < trajectory.size() - 1; idx++)
        {
            Trajectory t1 = trajectory.get(idx);
            double radius = computeRadius(t1, trajectory.get(idx + 1));
//            original formula where only curvature kappa is known:
//            double kappa = 1 / radius;
//            double maxCentripetal = Math.sqrt(A_MAX_LATERAL / kappa);
//            Reducible to due to curvature radius relation:
            double maxCentripetal = Math.sqrt(A_MAX_LATERAL * radius);
            double velocity = Math.min(_maxSpeed, maxCentripetal);
            t1.setVelocity(velocity);
        }
    }

    private double computeRadius(Trajectory t1, Trajectory t2)
    {
        Vector2D perpendicular1 = t1.getVector().getMiddlePerpendicular();
        Vector2D perpendicular2 = t2.getVector().getMiddlePerpendicular();
        Position2D circleCenter = Vector2D.unrangedIntersect(perpendicular1, perpendicular2);
        double radius = Double.MAX_VALUE;
        if(circleCenter != null)
        {
            radius = Position2D.distance(circleCenter, t1.getVector().getBase());
        }
        return radius;
    }

    private List<Trajectory> filterOutType(List<Trajectory> trajectories, TrajectoryType type)
    {
        return trajectories.stream().filter(t -> t.hasType(type)).collect(Collectors.toList());
    }
}
