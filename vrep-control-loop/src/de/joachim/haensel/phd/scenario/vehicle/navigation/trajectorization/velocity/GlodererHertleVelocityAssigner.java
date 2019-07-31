package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.ICurvatureChangeListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.IProfileChangeListener;

/**
 * This is a partial implementation of an algorithm desribed in "Spline-based Trajectory Optimization for Autonomous Vehicles with Ackerman drive" by Martiin Gloderer and Andreas Hertle.
 * The most important part (making the input a bezier-curve) is not part of this, let's see if it works anyway
 * Only the initial velocity assignment is made, the optimization (core part of the paper) is not realized.
 * @author dummy
 *
 */
public class GlodererHertleVelocityAssigner implements IVelocityAssigner
{
    // TODO check out maximum lateral acceleration that makes sense
    // maximum lateral acceleration. I made this the max centripedal acceleration
    private double _accelerationMaxLateral = 3.0;
    // maximum longitudinal acceleration -> throttle
    private double _accelerationMaxLongitudinal = 8.0;
    // maximum longitudinal deceleration -> brake
    private double _decelerationMaxLongitudinal = 8.0;
    // maximum longitudinal velocity
    private double _velocityMaxLongitudinal;
    
    private double _segmentSize;
    private List<IProfileChangeListener> _profileChangeListener;
    private List<ICurvatureChangeListener> _curvatureChangeListener;

    public GlodererHertleVelocityAssigner(double segmentSize, double maxSpeed)
    {
        _velocityMaxLongitudinal = maxSpeed;
        _segmentSize = segmentSize;
        _profileChangeListener = new ArrayList<>();
        _curvatureChangeListener = new ArrayList<>();
    }

    public GlodererHertleVelocityAssigner(double segmentSize, double maxVelocity, double maxLateralAcc, double maxLongAcc, double maxLongDec)
    {
        this(segmentSize, maxVelocity);
        _accelerationMaxLateral = maxLateralAcc;
        _accelerationMaxLongitudinal = maxLongAcc;
        _decelerationMaxLongitudinal = maxLongDec;
    }


    @Override
    public void addVelocities(List<TrajectoryElement> trajectories)
    {
        computeCurvatures(trajectories);
        notifyListeners(trajectories);

        trajectories.get(0).setVelocity(UnitConverter.kilometersPerHourToMetersPerSecond(1.0));
        forwardPass(trajectories);
        // TODO re-think this: for now I gave the car a little bump for the last segment so it can reach it's target if the veocity was set to 0
        trajectories.get(trajectories.size() - 1).setVelocity(UnitConverter.kilometersPerHourToMetersPerSecond(1.0));
        notifyListeners(trajectories);
        backwardPass(trajectories);
        notifyListeners(trajectories);
    }

    private void backwardPass(List<TrajectoryElement> trajectories)
    {
        for(int idx = trajectories.size() - 2; idx > 1; idx--)
        {
            TrajectoryElement t_j = trajectories.get(idx);
            TrajectoryElement t_j_plusone = trajectories.get(idx + 1);
            double v_j_plus1 = t_j_plusone.getVelocity();
            double s = Position2D.distance(t_j.getVector().getBase(), t_j_plusone.getVector().getBase());

            double velocity = min(t_j.getVelocity(), sqrt(sqr(v_j_plus1) + 2 * _accelerationMaxLongitudinal * s));
            t_j.setVelocity(velocity);
        }
    }

    private void forwardPass(List<TrajectoryElement> trajectories)
    {
        for(int idx = 1; idx < trajectories.size() - 2; idx++)
        {
            TrajectoryElement t_j_minusone = trajectories.get(idx);
            TrajectoryElement t_j = trajectories.get(idx + 1);
            double v_j_minus1 = t_j_minusone.getVelocity();
            double s = Position2D.distance(t_j.getVector().getBase(), t_j_minusone.getVector().getBase());
            
            double vel_maxCentripedal = min(_velocityMaxLongitudinal, sqrt(_accelerationMaxLateral * t_j.getRadius()));

            double vel_forward = min(vel_maxCentripedal, sqrt(sqr(v_j_minus1 + 2 * _accelerationMaxLongitudinal * s)));
            t_j.setVelocity(vel_forward);
        }
    }
    
    private double sqr(double val)
    {
        return val * val;
    }

    private void computeCurvatures(List<TrajectoryElement> trajectory)
    {
        for(int idx = 0; idx < trajectory.size() - 1; idx++)
        {
            TrajectoryElement t1 = trajectory.get(idx);
            TrajectoryElement t2 = trajectory.get(idx + 1);
            double radius = computeRadius(t1, t2);
            double kappa = 1 / radius;
            t1.setRadius(radius);
            t1.setKappa(kappa);
        }
    }

    private double computeRadius(TrajectoryElement t1, TrajectoryElement t2)
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

    @Override
    public void addProfileChangeListener(IProfileChangeListener listener)
    {
        _profileChangeListener.add(listener);
    }
    
    private void notifyListeners(List<TrajectoryElement> trajectories)
    {
        _profileChangeListener.forEach(l -> l.notifyChange(trajectories));
    }

    @Override
    public void addCurvatureChangeListener(ICurvatureChangeListener curveListener)
    {
        _curvatureChangeListener.add(curveListener);
    }

    private void notifyCurvatureListeners(Deque<Vector2D> curvatures)
    {
        _curvatureChangeListener.forEach(l -> l.notifyChange(curvatures));
    }
}
