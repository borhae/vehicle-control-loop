package de.joachim.haensel.vehicle;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.SegmentBuffer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;
import de.joachim.haensel.vehiclecontrol.Navigator;

public class NavigationController implements IUpperLayerControl
{
    private RoadMap _roadMap;
    private IActuatingSensing _sensorsActuators;
    private SegmentBuffer _segmentBuffer;
    private double _segmentSize;
    private List<ISegmentBuildingListener> _segmentBuildingListeners;
    
    public NavigationController(double segmentSize)
    {
        _segmentSize = segmentSize;
        _segmentBuildingListeners = new ArrayList<ISegmentBuildingListener>();
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
        double maxVelocity = 30.0;
        _roadMap = roadMap;
        Position2D currentPosition = _sensorsActuators.getNonDynamicPosition();
        Navigator navigator = new Navigator(_roadMap);
        navigator.addSegmentBuildingListeners(_segmentBuildingListeners);
        List<Line2D> routeBasis = navigator.getRoute(currentPosition, targetPosition);
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityAssignerFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, maxVelocity);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityAssignerFactory , _segmentSize);
        trajectorizer.addSegmentBuildingListeners(_segmentBuildingListeners);
        _segmentBuffer.fillBuffer(trajectorizer.createTrajectory(routeBasis));
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
}
