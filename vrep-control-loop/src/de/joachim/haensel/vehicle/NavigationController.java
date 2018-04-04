package de.joachim.haensel.vehicle;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.interpolation.IterativeInterpolationTrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Route;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehiclecontrol.Navigator;

public class NavigationController implements ITopLayerControl
{
    private RoadMap _roadMap;
    private IActuatingSensing _sensorsActuators;
    private Route _currentRoute;
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
        
        _currentRoute = new Route();
    }

    @Override
    public void buildSegmentBuffer(Position2D targetPosition, RoadMap roadMap)
    {
        _roadMap = roadMap;
        _sensorsActuators.computeAndLockSensorData();
        Position2D currentPosition = _sensorsActuators.getPosition();
        Navigator navigator = new Navigator(_roadMap);
        navigator.addSegmentBuildingListeners(_segmentBuildingListeners);
        List<Line2D> routeBasis = navigator.getRoute(currentPosition, targetPosition);
        
        IterativeInterpolationTrajectorizer trajectorizer = new IterativeInterpolationTrajectorizer(_segmentSize);
        trajectorizer.addSegmentBuildingListeners(_segmentBuildingListeners);
        _currentRoute.createRoute(trajectorizer.createTrajectory(routeBasis));
    }

    @Override
    public List<Trajectory> getNewSegments(int requestSize)
    {
        return _currentRoute.getSegments(requestSize);
    }

    public Trajectory segmentsPeek()
    {
        return _currentRoute.peek();
    }

    public int getSegmentBufferSize()
    {
        return _currentRoute.getSize();
    }
        
    public void addSegmentBuilderListener(ISegmentBuildingListener listener)
    {
        _segmentBuildingListeners.add(listener);
    }
}
