package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;

public class RandomAdaptiveNavigationController implements IUpperLayerControl 
{
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
    private MersenneTwister _randomGenerator;
    
    private RandomAdaptiveNavigationController(double segmentSize, double maxVelocity)
    {
        _randomGenerator = new MersenneTwister(4096);
        _segmentSize = segmentSize;
        _routeBuildingListeners = new ArrayList<IRouteBuildingListener>();
        _maxVelocity = maxVelocity;
        _debuggingParameters = new DebugParams();
        _maxLateralAcceleration = MAX_LATERAL_ACCELERATION;
        _maxLongitudinalAcceleration = MAX_LONGITUDINAL_ACCELERATION;
        _maxLongitudinalDecceleration = MAX_LONGITUDINAL_DECCELERATION;
    }

    public RandomAdaptiveNavigationController(double segmentSize, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration)
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
        // with a new velocity assigner every run, we adapt the acceleration behavior
        double randomFactor = (1 - 2 *_randomGenerator.nextDouble()); // yields values [-1, 1]
        double maxVel = _maxVelocity;
        double maxLatAcc = _maxLateralAcceleration + randomFactor * 0.5;
        double maxLongAcc = _maxLongitudinalAcceleration + randomFactor * 0.2;
        double maxLongDec = _maxLongitudinalDecceleration + randomFactor * 0.5;
        _velocityAssignerFactory = segSize -> {return new BasicVelocityAssigner(segSize, maxVel, maxLatAcc, maxLongAcc, maxLongDec);};
        _roadMap = roadMap;
        Position2D currentPosition = _sensorsActuators.getNonDynamicPosition();
        Vector2D orientation = _sensorsActuators.getOrientation();
        Navigator navigator = new Navigator(_roadMap);
        navigator.addRouteBuildingListeners(_routeBuildingListeners);
        List<Line2D> routeBasis = navigator.getRouteWithInitialOrientation(currentPosition, targetPosition, orientation);
        _debuggingParameters.notifyNavigationListenersRouteChanged(routeBasis);
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, _velocityAssignerFactory , _segmentSize);
        trajectorizer.addSegmentBuildingListeners(_routeBuildingListeners);
        List<TrajectoryElement> allSegments = trajectorizer.createTrajectory(routeBasis);
        _segmentBuffer.fillBuffer(allSegments);
        _debuggingParameters.notifyNavigationListenersSegmentsChanged(allSegments, currentPosition, targetPosition);
    }

    @Override
    public List<TrajectoryElement> getNewElements(int requestSize)
    {
        return _segmentBuffer.getSegments(requestSize);
    }

    public TrajectoryElement segmentsPeek()
    {
        return _segmentBuffer.peek();
    }

    public int getSegmentBufferSize()
    {
        return _segmentBuffer.getSize();
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
	public boolean segmentsLeft() 
	{
		return _segmentBuffer.getSize() > 0;
	}

    @Override
    public boolean hasElements(int elementRequestSize)
    {
        return _segmentBuffer.getSize() >= elementRequestSize;
    }
}
