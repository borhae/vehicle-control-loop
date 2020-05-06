package de.joachim.haensel.phd.scenario.vehicle.navigation.adaptable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable.AtomicSetActualError;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.SegmentBuffer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;

public class AdaptingNavigationController implements IUpperLayerControl 
{
    public enum AdaptationDirections
    {
        STAY, RISE, FALL
    }

    private static final double MAX_LONGITUDINAL_DECCELERATION = 8.0;
    private static final double MAX_LONGITUDINAL_ACCELERATION = 2.0;
    private static final double MAX_LATERAL_ACCELERATION = 1.5;
    private RoadMap _roadMap;
    private IActuatingSensing _sensorsActuators;
    private SegmentBuffer _segmentBuffer;
    private double _segmentSize;
    private List<IRouteBuildingListener> _routeBuildingListeners;
    private DebugParams _debuggingParameters;
    private double _maxVelocity;
    private double _maxLateralAcceleration;
    private double _maxLongitudinalAcceleration;
    private double _maxLongitudinalDecceleration;
    private IVelocityAssignerFactory _velocityAssignerFactory;
    private AdaptationDirections _adaptationDir;
    private double _adaptationThreshold;
    
    public AdaptingNavigationController(double segmentSize, double maxVelocity)
    {
        _segmentSize = segmentSize;
        _routeBuildingListeners = new ArrayList<IRouteBuildingListener>();
        _maxVelocity = maxVelocity;
        _debuggingParameters = new DebugParams();
        _maxLateralAcceleration = MAX_LATERAL_ACCELERATION;
        _maxLongitudinalAcceleration = MAX_LONGITUDINAL_ACCELERATION;
        _maxLongitudinalDecceleration = MAX_LONGITUDINAL_DECCELERATION;
        _adaptationDir = AdaptationDirections.STAY;
        _adaptationThreshold = 1.0; 
        _velocityAssignerFactory = segSize -> {return new BasicVelocityAssigner(segSize, _maxVelocity, _maxLateralAcceleration, _maxLongitudinalAcceleration, _maxLongitudinalDecceleration);};
    }

    public AdaptingNavigationController(double segmentSize, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration)
    {
        this(segmentSize, maxVelocity);
        _maxLateralAcceleration = maxLateralAcceleration;
        _maxLongitudinalAcceleration = maxLongitudinalAcceleration;
        _maxLongitudinalDecceleration = maxLongitudinalDecceleration;
    }

    @Override
    public void initController(IActuatingSensing sensorsActuators, RoadMap roadMap)
    {
        _sensorsActuators = sensorsActuators;
        _roadMap = roadMap;
        _segmentBuffer = new SegmentBuffer();
    }

    @Override
    public void buildSegmentBuffer(Position2D targetPosition, RoadMap roadMap)
    {
        _roadMap = roadMap;
        Position2D currentPosition = _sensorsActuators.getNonDynamicPosition();
        Vector2D orientation = _sensorsActuators.getOrientation();
        Navigator navigator = new Navigator(_roadMap);
        navigator.addRouteBuildingListeners(_routeBuildingListeners);
        List<Line2D> routeBasis = navigator.getRouteWithInitialOrientation(currentPosition, targetPosition, _segmentSize, orientation);
        _debuggingParameters.notifyNavigationListenersRouteChanged(routeBasis);
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, _velocityAssignerFactory , _segmentSize);
        trajectorizer.addSegmentBuildingListeners(_routeBuildingListeners);
        List<TrajectoryElement> allSegments = trajectorizer.createTrajectory(routeBasis);
        _segmentBuffer.fillBuffer(allSegments);
        _debuggingParameters.notifyNavigationListenersSegmentsChanged(allSegments, currentPosition, targetPosition);
    }

    @Override
    public List<TrajectoryElement> getNewElements(int requestSize, AtomicSetActualError error)
    {
        List<TrajectoryElement> preAdapted = _segmentBuffer.getSegments(requestSize);
        List<TrajectoryElement> postAdapted = preAdapted.stream().map(traj -> traj.deepCopy()).collect(Collectors.toList());
        double angleErr = error.getAngleErr();
        double distanceErr = error.getDistanceErr();
        double errorSum = angleErr + distanceErr;
        _adaptationDir = errorSum < _adaptationThreshold ? AdaptationDirections.RISE : (errorSum > _adaptationThreshold ? AdaptationDirections.FALL : AdaptationDirections.STAY); 
        switch (_adaptationDir)
        {
            case RISE:
                postAdapted = increaseVelocity(preAdapted, errorSum);
                break;
            case FALL:
                postAdapted = decreaseVelocity(preAdapted, errorSum);
            case STAY:
            default:
                break;
        }
        return postAdapted;
    }

    private List<TrajectoryElement> decreaseVelocity(List<TrajectoryElement> trajectory, double errorSum)
    {
        trajectory.forEach(elem -> elem.setVelocity(elem.getVelocity() - errorSum));
        return trajectory;
    }

    private List<TrajectoryElement> increaseVelocity(List<TrajectoryElement> trajectory, double errorSum)
    {
        trajectory.forEach(elem -> elem.setVelocity(elem.getVelocity() + errorSum));
        return trajectory;
    }

    public TrajectoryElement segmentsPeek()
    {
        return _segmentBuffer.peek();
    }
    
    @Override
    public boolean hasElements(int requestSize)
    {
        return _segmentBuffer.getSize() >= requestSize;
    }

    public int getSegmentBufferSize()
    {
        return _segmentBuffer.getSize();
    }
    
    @Override
	public boolean segmentsLeft() 
    {
		return _segmentBuffer.getSize() > 0;
	}

	@Override
    public void addRouteBuilderListener(IRouteBuildingListener listener)
    {
        _routeBuildingListeners.add(listener);
    }

    @Override
    public void activateDebugging(DebugParams debuggingParameters)
    {
        _debuggingParameters = debuggingParameters;
    }

    @Override
    public void deactivateDebugging()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public double getTrajectoryElementLength()
    {
        return _segmentSize;
    }
}