package de.joachim.haensel.vehicle;

import java.util.Timer;
import java.util.TimerTask;

import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import sumobindings.LaneType;

public class NavigationController implements ITopLayerControl
{
    public class ControlLoop extends TimerTask
    {

        @Override
        public void run()
        {
            
        }
    }

    public enum NavigationState
    {
        IDLE
    }

    private static final long UPDATE_FREQUENCY = 200; //every 200 millis check on the lower layer and possibly feed new instructions

    private ILowLevelController _controllee;
    private NavigationState _navigationState;
    private RoadMap _roadMap;
    private IActuatingSensing _sensorsActuators;
    private Timer _timer;


    public NavigationController(ILowLevelController lowerControlLayer, IActuatingSensing sensorsActuators, RoadMap roadMap)
    {
        _controllee = lowerControlLayer;
        _navigationState = NavigationState.IDLE;
        _timer = new Timer();
        _sensorsActuators = sensorsActuators;
        _roadMap = roadMap;
    }

    @Override
    public void driveTo(Position2D targetPosition)
    {
        Position2D currentPosition = _sensorsActuators.getPosition();
        createSegments(currentPosition, targetPosition);
        _timer.schedule(new ControlLoop(), UPDATE_FREQUENCY);
    }

    private void createSegments(Position2D currentPosition, Position2D position)
    {
        //TODO pick up here, when closest lanes can be determined the navigation can start
       LaneType lane = _roadMap.getClosestLaneFor(currentPosition);
    }

    @Override
    public void driveToBlocking(Position2D target)
    {
        driveTo(target);
    }
}
