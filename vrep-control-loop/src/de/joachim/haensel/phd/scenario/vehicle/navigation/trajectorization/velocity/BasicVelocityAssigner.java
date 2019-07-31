package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement.VelocityEdgeType;


/**
 * The algorithm is based on a paper by Tianyu Gu, Jarrod Snider, John M. Dolan and Jin-woo Lee
 * Focused Trajectory Planning for Autonomous On-Road Driving
 * doi:  10.1109/IVS.2013.6629524
 */
public class BasicVelocityAssigner implements IVelocityAssigner
{
    public interface ICurvatureChangeListener
    {
        public void notifyChange(Deque<Vector2D> curvatures);
    }

    public interface IProfileChangeListener
    {
        public void notifyChange(List<TrajectoryElement> trajectories);
    }


    // TODO check out maximum lateral acceleration that makes sense
    // maximum lateral acceleration
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
    private double _initialVelocity;

    public BasicVelocityAssigner(double segmentSize, double maxSpeed)
    {
        _velocityMaxLongitudinal = maxSpeed;
        _segmentSize = segmentSize;
        _profileChangeListener = new ArrayList<>();
        _curvatureChangeListener = new ArrayList<>();
        _initialVelocity = 2.0;
    }
    
    public BasicVelocityAssigner(double segmentSize, double maxVelocity, double maxLateralAcc, double maxLongAcc, double maxLongDec)
    {
        this(segmentSize, maxVelocity);
        _accelerationMaxLateral = maxLateralAcc;
        _accelerationMaxLongitudinal = maxLongAcc;
        _decelerationMaxLongitudinal = maxLongDec;
    }
    
    public void setInitialVelocity(double initialVelocity)
    {
        _initialVelocity = initialVelocity;
    }

    @Override
    public void addVelocities(List<TrajectoryElement> trajectories)
    {
        computeCurvatures(trajectories);
        notifyListeners(trajectories);

        curvatureLimitedPass(trajectories);
        trajectories.get(0).setVelocity(_initialVelocity);
//        lowPassFilter(trajectories);
        notifyListeners(trajectories);
        
        identifyRegions(trajectories);
        fillInitialPadding(trajectories);
        flattenFinalPart(trajectories);
        notifyListeners(trajectories);
        capAccelerationDeceleration(trajectories);
        notifyListeners(trajectories);
        
//        lowPassFilter(trajectories);
//        notifyListeners(trajectories);
    }

    private void flattenFinalPart(List<TrajectoryElement> trajectories)
    {
        if(trajectories.size() >= 5)
        {
            List<TrajectoryElement> lastElements = trajectories.subList(trajectories.size() - 5, trajectories.size());
            lastElements.stream().forEach(elem -> elem.setVelocity(Math.min(elem.getVelocity(), 2.0)));
        }
    }

    private void capAccelerationDeceleration(List<TrajectoryElement> trajectories)
    {
        double s_i = _segmentSize / 2.0; // this is approximately true (s_i traveldistance between two points)
        double threshold = 0.1;
        double curProfileChange = Double.MAX_VALUE;
        notifyListeners(trajectories);
        System.out.println("Velocity assignement: ");
        while(curProfileChange > threshold)
        {
            System.out.print(".");
            double curMaxChange = 0.0;
            for (int idx = 0; idx < trajectories.size() - 1; idx++)
            {
                TrajectoryElement curTrajectory = trajectories.get(idx);
                TrajectoryElement nextTrajectory = trajectories.get(idx + 1); 
                double v_i_sq = curTrajectory.getVelocity() * curTrajectory.getVelocity();
                double v_i_p1_sq = nextTrajectory.getVelocity() * nextTrajectory.getVelocity();
                double delta = 0.0;
//                if(curTrajectory.getRiseFall() == VelocityEdgeType.RAISE)
                if(curTrajectory.getVelocity() < nextTrajectory.getVelocity())
                {
                    double acc = (v_i_p1_sq - v_i_sq) / (2 * s_i);
                    if(acc >= _accelerationMaxLongitudinal)
                    {
                        double newVelocity = Math.sqrt(v_i_sq + 2 * s_i *_accelerationMaxLongitudinal);
                        delta = nextTrajectory.getVelocity() - newVelocity;
                        nextTrajectory.setVelocity(newVelocity);
                    }
                } 
//                else if(curTrajectory.getRiseFall() == VelocityEdgeType.FALL)
                else if(curTrajectory.getVelocity() > nextTrajectory.getVelocity())
                {
                    double dec = (v_i_sq - v_i_p1_sq) / (2 * s_i);
                    if(dec >= _decelerationMaxLongitudinal)
                    {
                        double newVelocity = Math.sqrt(v_i_p1_sq + 2 * s_i * _decelerationMaxLongitudinal);
                        delta = curTrajectory.getVelocity() - newVelocity;
                        curTrajectory.setVelocity(newVelocity);
                    }
                }
                if(Math.abs(delta) > curMaxChange)
                {
                    curMaxChange = delta;
                }
            }
            curProfileChange = curMaxChange;
            notifyListeners(trajectories);
        }
    }

    private void fillInitialPadding(List<TrajectoryElement> trajectories)
    {
        TrajectoryElement nextTrajectory = null;
        for (int idx = trajectories.size() - 1; idx >= 0; idx--)
        {
            TrajectoryElement curTrajectory = trajectories.get(idx);
            if((nextTrajectory != null) && (curTrajectory.getRiseFall() == VelocityEdgeType.START))
            {
                curTrajectory.setRiseFall(nextTrajectory.getRiseFall());
            }
            nextTrajectory = curTrajectory;
        }
    }

    private void identifyRegions(List<TrajectoryElement> trajectories)
    {
        for (int idx = 0; idx < trajectories.size() - 1; idx++)
        {
            TrajectoryElement lastTrajectory = idx == 0 ? null : trajectories.get(idx - 1);
            TrajectoryElement curTrajectory = trajectories.get(idx);
            TrajectoryElement nextTrajectory = trajectories.get(idx + 1);
            if(nextTrajectory.getVelocity() > curTrajectory.getVelocity())
            {
                curTrajectory.setRiseFall(VelocityEdgeType.RAISE);
            }
            else if(nextTrajectory.getVelocity() < curTrajectory.getVelocity())
            {
                curTrajectory.setRiseFall(VelocityEdgeType.FALL);
            }
            else
            {
                if(lastTrajectory == null)
                {
                    curTrajectory.setRiseFall(VelocityEdgeType.START);
                }
                else
                {
                    curTrajectory.setRiseFall(lastTrajectory.getRiseFall());
                }
            }
        }
        
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

    private void curvatureLimitedPass(List<TrajectoryElement> trajectories)
    {
//            original formula where only curvature kappa is known:
//            double maxCentripetal = Math.sqrt(A_MAX_LATERAL / kappa);
//            Reducible to due to curvature radius relation:
        Consumer<TrajectoryElement> curvatureToVelocity = t -> 
        {
            double maxCentripedal = Math.sqrt(0.1 * _accelerationMaxLateral * Math.pow(t.getRadius(), 1.5));
            double velocity = Math.min(_velocityMaxLongitudinal, maxCentripedal);
            if(t.isReverse())
            {
                velocity = 0.1;
            }
            
            t.setVelocity(velocity);
        };
        trajectories.forEach(curvatureToVelocity);
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

