package de.joachim.haensel.vehicle;

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
    public enum NavigationState
    {
        IDLE, DRIVING
    }

    private NavigationState _navigationState;
    private RoadMap _roadMap;
    private IActuatingSensing _sensorsActuators;
    private Route _currentRoute;

    public NavigationController(IActuatingSensing sensorsActuators, RoadMap roadMap)
    {
        _navigationState = NavigationState.IDLE;
        _sensorsActuators = sensorsActuators;
        _roadMap = roadMap;
        
        _currentRoute = new Route();
    }

    @Override
    public void driveTo(Position2D targetPosition, RoadMap roadMap)
    {
        _roadMap = roadMap;
        Position2D currentPosition = _sensorsActuators.getPosition();
        Navigator navigator = new Navigator(_roadMap);
        List<Line2D> routeBasis = navigator.getRoute(currentPosition, targetPosition);
        
        IterativeInterpolationTrajectorizer trajectorizer = new IterativeInterpolationTrajectorizer(2.0);
        _currentRoute.createRoute(trajectorizer.createTrajectory(routeBasis));
        // TODO pick up here, when there is a trajectory, that we can follow
        _navigationState = NavigationState.DRIVING;
    }

    @Override
    public void driveToBlocking(Position2D target, RoadMap roadMap)
    {
        driveTo(target, roadMap);
    }

    @Override
    public List<Trajectory> getNewSegments(int requestSize)
    {
        return _currentRoute.getSegments(requestSize);
    }
}
