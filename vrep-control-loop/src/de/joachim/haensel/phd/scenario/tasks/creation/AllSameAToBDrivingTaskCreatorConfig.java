package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.tasks.DriveAtoBTask;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.SimStartTask;
import de.joachim.haensel.phd.scenario.tasks.SimStopTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleBuildTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStopDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStopTask;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class AllSameAToBDrivingTaskCreatorConfig implements ITaskCreatorConfig, IDrivingTask
{
    private int _numOfTasks;
    private double _xSource;
    private double _ySource;
    private double _xTarget;
    private double _yTarget;
    private int _timeoutSec;
    private List<ITask> _tasks;
    private boolean _debug;
    private Iterator<ITask> _taskIterator;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private RoadMap _map;
    private double _lookahead;
    private double _maxVelocity;
    private double _maxLongitudinalAcceleration;
    private double _maxLongitudinalDecceleration;
    private double _maxLateralAcceleration;
    
    public AllSameAToBDrivingTaskCreatorConfig(int numOfTasks, boolean debug)
    {
        _debug = debug;
        _numOfTasks = numOfTasks;
        _lookahead = DEFAULT_PURE_PURSUIT_LOOKAHEAD;
        _maxVelocity = DEFAULT_MAX_VELOCITY;
        _maxLongitudinalAcceleration = DEFAULT_MAX_LONGITUDINAL_ACCELERATION;
        _maxLongitudinalDecceleration = DEFAULT_MAX_LONGITUDINAL_DECCELERATION;
        _maxLateralAcceleration = DEFAULT_MAX_LATERAL_ACCELERATION;
    }

    public void setAllSame(double xSource, double ySource, double xTarget, double yTarget)
    {
        _xSource = xSource;
        _ySource = ySource;
        _xTarget = xTarget;
        _yTarget = yTarget;
        _timeoutSec = ITaskCreatorConfig.estimateTimeout(xSource, ySource, xTarget, yTarget);
    }
    
    public void configRoutesOnMap(double xSource, double ySource, double xTarget, double yTarget)
    {
        Position2D source = _map.getClosestPointOnMap(new Position2D(xSource, ySource));
        Position2D target = _map.getClosestPointOnMap(new Position2D(xTarget, yTarget));
        _xSource = source.getX();
        _ySource = source.getY();
        _xTarget = target.getX();
        _yTarget = target.getY();
        _timeoutSec = ITaskCreatorConfig.estimateTimeout(xSource, ySource, xTarget, yTarget);
    }

    public void configSimulator(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
    }

    public void setMap(RoadMap map)
    {
        _map = map;
    }

    @Override
    public void init()
    {
        _tasks = new ArrayList<>();
        for(int cnt = 0; cnt < _numOfTasks; cnt++)
        {
            Position2D startPosition = new Position2D(_xSource, _ySource);
            Vector2D orientation = IDrivingTask.computeOrientation(_map, startPosition, new Position2D(_xTarget, _yTarget));
            VehicleBuildTask vehicleBuildTask = new VehicleBuildTask(_vrep, _clientID, _objectCreator, _map, startPosition, orientation, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm");
            vehicleBuildTask.setControlParams(_lookahead, _maxVelocity, _maxLongitudinalAcceleration, _maxLongitudinalDecceleration, _maxLateralAcceleration);
            _tasks.add(vehicleBuildTask);
            _tasks.add(new SimStartTask(_vrep, _clientID));
            _tasks.add(new VehicleStartTask(vehicleBuildTask));
            if(_debug)
            {
                _tasks.add(new VehicleStartDebugTask(_objectCreator, vehicleBuildTask));
            }
            _tasks.add(new DriveAtoBTask(_xSource, _ySource, _xTarget, _yTarget, _timeoutSec, vehicleBuildTask, _map));
            if(_debug)
            {
                _tasks.add(new VehicleStopDebugTask(vehicleBuildTask));
            }
            _tasks.add(new VehicleStopTask(vehicleBuildTask));
            _tasks.add(new SimStopTask(_vrep, _clientID));
            _taskIterator = _tasks.iterator();
        }
    }
    
    @Override
    public boolean hasNext()
    {
        return _taskIterator.hasNext();
    }

    @Override
    public ITask getNext()
    {
        return _taskIterator.next();
    }

    @Override
    public void setControlParams(double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration)
    {
        _lookahead = lookahead;
        _maxVelocity = maxVelocity;
        _maxLongitudinalAcceleration = maxLongitudinalAcceleration;
        _maxLongitudinalDecceleration = maxLongitudinalDecceleration;
        _maxLateralAcceleration = maxLateralAcceleration;
    }
}