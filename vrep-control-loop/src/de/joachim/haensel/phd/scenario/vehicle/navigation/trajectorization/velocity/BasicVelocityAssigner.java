package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory.VelocityEdgeType;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryType;


/**
 * The algorithm is based on a paper by Tianyu Gu, Jarrod Snider, John M. Dolan and Jin-woo Lee
 * Focused Trajectory Planning for Autonomous On-Road Driving
 * doi:  10.1109/IVS.2013.6629524
 */
public class BasicVelocityAssigner implements IVelocityAssigner
{
    private static final double ALPHA_SMOOTHING = 1.0;

    public interface ICurvatureChangeListener
    {
        public void notifyChange(Deque<Vector2D> curvatures);
    }

    public interface IProfileChangeListener
    {
        public void notifyChange(List<Trajectory> trajectories);
    }


    // TODO check out maximum lateral acceleration that makes sense
    // maximum lateral acceleration
    private double _accelerationMaxLateral = 3.0;
    // maximum longitudinal acceleration -> throttle
    private double _accelerationMaxLongitudinal = 2.0;
    // maximum longitudinal deceleration -> brake
    private double _decelerationMaxLongitudinal = 8.0;
    // maximum longitudinal velocity
    private double _velocityMaxLongitudinal;
    
    private double _segmentSize;
    private List<IProfileChangeListener> _profileChangeListener;
    private List<ICurvatureChangeListener> _curvatureChangeListener;

    public BasicVelocityAssigner(double segmentSize, double maxSpeed)
    {
        _velocityMaxLongitudinal = maxSpeed;
        _segmentSize = segmentSize;
        _profileChangeListener = new ArrayList<>();
        _curvatureChangeListener = new ArrayList<>();
    }
    
    public BasicVelocityAssigner(double segmentSize, double maxVelocity, double maxLateralAcc, double maxLongAcc, double maxLongDec)
    {
        this(segmentSize, maxVelocity);
        _accelerationMaxLateral = maxLateralAcc;
        _accelerationMaxLongitudinal = maxLongAcc;
        _decelerationMaxLongitudinal = maxLongDec;
    }

    @Override
    public void addVelocities(List<Trajectory> trajectories)
    {
        List<Trajectory> original = filterOutType(trajectories, TrajectoryType.OVERLAY);
        List<Trajectory> overlay = filterOutType(trajectories, TrajectoryType.ORIGINAL);
        computeCurvatures(original);
        notifyListeners(trajectories);

//      initial idea but the idea of overlapping segments doesn't really work out in conjunction with the curvature
//      curvatureLimitedPass(original);
//      curvatureLimitedPass(overlay);
        computeCurvatures(overlay);
        notifyListeners(trajectories);
        
        //remove the first element from the overlays to bring the indexing into order. 
        //first half segmentn in overlay is not needed anyway
        trajectories.remove(0);
        curvatureLimitedPass(trajectories);
        
        lowPassFilter(trajectories);
        notifyListeners(trajectories);
        
        identifyRegions(trajectories);
        fillInitialPadding(trajectories);
        capAccelerationDeceleration(trajectories);
        notifyListeners(trajectories);
        
        lowPassFilter(trajectories);
        notifyListeners(trajectories);
    }

    private void lowPassFilter(List<Trajectory> trajectories)
    {
        //v_i
        double[] velocities_i = new double[trajectories.size()];
        trajectories.stream().map(IndexAdder.indexed()).forEachOrdered(e -> velocities_i[e.idx()] = e.v().getVelocity());

        //v_i-1
        double[] velocities_im1 = new double[trajectories.size() + 1];
        trajectories.stream().map(IndexAdder.indexed()).forEachOrdered(e -> velocities_im1[e.idx() + 1] = e.v().getVelocity());
        velocities_im1[0] = velocities_i[0]; // add first as estimate for nonexisting previous
        
        for(int idx = 0; idx < velocities_i.length; idx++)
        {
            velocities_im1[idx] = velocities_im1[idx] + 0.2 * (velocities_i[idx] - velocities_im1[idx]);
        }
        System.out.println("check");       
//        for(int idx = 1; idx < trajectories.size(); idx++)
//        {
//            Trajectory last = trajectories.get(idx - 1);
//            Trajectory current = trajectories.get(idx);
//            double distance = last.getVector().getBase().distance(current.getVector().getBase());
//            
////            double newValue = last.getVelocity() + distance * ALPHA_SMOOTHING * (current.getVelocity() - last.getVelocity());
//            double v_i = last.getVelocity();
//            double v_ip1 = current.getVelocity();
//            v_i = v_i + (v_ip1 - v_i);
//            last.setVelocity(v_i);
//        }
    }

    private void capAccelerationDeceleration(List<Trajectory> trajectories)
    {
        double s_i = _segmentSize / 2.0; // this is approximately true (s_i traveldistance between two points)
        double threshold = 0.1;
        double curProfileChange = Double.MAX_VALUE;
        int iterationCnt = 1;
        while(curProfileChange > threshold)
        {
            System.out.println("iteration: " + iterationCnt + " --------------------------------------------- ");
            double curMaxChange = 0.0;
            for (int idx = 0; idx < trajectories.size() - 1; idx++)
            {
                Trajectory curTrajectory = trajectories.get(idx);
                Trajectory nextTrajectory = trajectories.get(idx + 1); 
                double v_i_sq = curTrajectory.getVelocity() * curTrajectory.getVelocity();
                double v_i_p1_sq = nextTrajectory.getVelocity() * nextTrajectory.getVelocity();
                double delta = 0.0;
                if(curTrajectory.getRiseFall() == VelocityEdgeType.RAISE)
                {
                    double acc = (v_i_p1_sq - v_i_sq) / (2 * s_i);
                    if(acc >= _accelerationMaxLongitudinal)
                    {
                        double newVelocity = Math.sqrt(v_i_sq + 2 * s_i *_accelerationMaxLongitudinal);
                        delta = nextTrajectory.getVelocity() - newVelocity;
                        nextTrajectory.setVelocity(newVelocity);
                    }
                } 
                else if(curTrajectory.getRiseFall() == VelocityEdgeType.FALL)
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
            iterationCnt++;
            curProfileChange = curMaxChange;
            notifyListeners(trajectories);
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
        for (int idx = 0; idx < trajectories.size() - 1; idx++)
        {
            Trajectory lastTrajectory = idx == 0 ? null : trajectories.get(idx - 1);
            Trajectory curTrajectory = trajectories.get(idx);
            Trajectory nextTrajectory = trajectories.get(idx + 1);
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

    private void computeCurvatures(List<Trajectory> trajectory)
    {
        Deque<Vector2D> curvatures = new LinkedList<>();
        for(int idx = 0; idx < trajectory.size() - 1; idx++)
        {
            Trajectory t1 = trajectory.get(idx);
            Trajectory t2 = trajectory.get(idx + 1);
            double radius = computeRadius(t1, t2);
            double kappa = 1 / radius;
            t1.setRadius(radius);
            t1.setKappa(kappa);
            // just for debugging in graphical representation
            curvatures.addLast(t2.getVector().getMiddlePerpendicular().scale(- kappa * 10.0));
        }
        notifyCurvatureListeners(curvatures);
    }

    private void curvatureLimitedPass(List<Trajectory> trajectories)
    {
//            original formula where only curvature kappa is known:
//            double maxCentripetal = Math.sqrt(A_MAX_LATERAL / kappa);
//            Reducible to due to curvature radius relation:
        Consumer<Trajectory> curvatureToVelocity = t -> 
        {
            double maxCentripedal = Math.sqrt(_accelerationMaxLateral * t.getRadius());
            double velocity = Math.min(_velocityMaxLongitudinal, maxCentripedal);
            t.setVelocity(velocity);
        };
        trajectories.forEach(curvatureToVelocity);
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

    @Override
    public void addProfileChangeListener(IProfileChangeListener listener)
    {
        _profileChangeListener.add(listener);
    }
    
    private void notifyListeners(List<Trajectory> trajectories)
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

