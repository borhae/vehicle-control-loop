package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ISegmentBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;

public class DefaultNavigationController implements IUpperLayerControl
{
    private RoadMap _roadMap;
    private IActuatingSensing _sensorsActuators;
    private SegmentBuffer _segmentBuffer;
    private double _segmentSize;
    private List<ISegmentBuildingListener> _segmentBuildingListeners;
    private DebugParams _debuggingParameters;
    private double _maxSpeed;
    
    public DefaultNavigationController(double segmentSize, double maxSpeed)
    {
        _segmentSize = segmentSize;
        _segmentBuildingListeners = new ArrayList<ISegmentBuildingListener>();
        _maxSpeed = maxSpeed;
        _debuggingParameters = new DebugParams();
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
        Navigator navigator = new Navigator(_roadMap);
        navigator.addSegmentBuildingListeners(_segmentBuildingListeners);
        List<Line2D> routeBasis = navigator.getRoute(currentPosition, targetPosition);
        _debuggingParameters.notifyNavigationListenersRouteChanged(routeBasis);
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityAssignerFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, _maxSpeed, 6.0, 16.0, 16.0);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityAssignerFactory , _segmentSize);
        trajectorizer.addSegmentBuildingListeners(_segmentBuildingListeners);
        List<Trajectory> allSegments = trajectorizer.createTrajectory(routeBasis);
        _segmentBuffer.fillBuffer(allSegments);
        _debuggingParameters.notifyNavigationListenersSegmentsChanged(allSegments);
    }

    @Override
    public List<Trajectory> getNewSegments(int requestSize)
    {
        return _segmentBuffer.getSegments(requestSize);
    }

    public Trajectory segmentsPeek()
    {
        return _segmentBuffer.peek();
    }

    public int getSegmentBufferSize()
    {
        return _segmentBuffer.getSize();
    }
        
    public void addSegmentBuilderListener(ISegmentBuildingListener listener)
    {
        _segmentBuildingListeners.add(listener);
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
}
