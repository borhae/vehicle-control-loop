package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.tasks.AdditionalLowerControlLayerInitTask;
import de.joachim.haensel.phd.scenario.tasks.DriveAtoBTask;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.SimStartTask;
import de.joachim.haensel.phd.scenario.tasks.SimStopTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleBuildTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStopDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStopTask;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class PointListTaskCreatorConfig implements ITaskCreatorConfig, IDrivingTask
{
    protected List<Position2D> _targetPoints;
    protected List<ITask> _tasks;
    protected RoadMap _map;
    protected VRepRemoteAPI _vrep;
    protected int _clientID;
    protected VRepObjectCreation _objectCreator;
    protected boolean _debug;
    protected Iterator<ITask> _taskIterator;
    protected List<ILowerLayerControl> _lowerLayerControls;
    private List<INavigationListener> _navigationListeners;

    protected double _lookahead;
    protected double _maxVelocity;
    protected double _maxLongitudinalAcceleration;
    protected double _maxLongitudinalDecceleration;
    protected double _maxLateralAcceleration;
    private ILowerLayerFactory _lowerLayerFactory;
    private IUpperLayerFactory _upperLayerFactory;
    private String _carmodel;

    public PointListTaskCreatorConfig(boolean debug)
    {
        this();
        _debug = debug;
    }

    public PointListTaskCreatorConfig()
    {
        _lowerLayerFactory = null;
        _upperLayerFactory = null;
        _targetPoints = new ArrayList<>();
        _lowerLayerControls = new ArrayList<>();
        _lookahead = DEFAULT_PURE_PURSUIT_LOOKAHEAD;
        _maxVelocity = DEFAULT_MAX_VELOCITY;
        _maxLongitudinalAcceleration = DEFAULT_MAX_LONGITUDINAL_ACCELERATION;
        _maxLongitudinalDecceleration = DEFAULT_MAX_LONGITUDINAL_DECCELERATION;
        _maxLateralAcceleration = DEFAULT_MAX_LATERAL_ACCELERATION;
    }

    public void setTargetPoints(List<Position2D> targetPoints)
    {
        _targetPoints = targetPoints;
    }

    @Override
    public ITask getNext()
    {
        return _taskIterator.next();
    }

    @Override
    public boolean hasNext()
    {
        return _taskIterator.hasNext();
    }

    @Override
    public void init()
    {
        _tasks = new ArrayList<>();
        if(_targetPoints.size() <= 1)
        {
            return;
        }
        Position2D startPosition = _targetPoints.get(0);
        
        Vector2D orientation = IDrivingTask.computeOrientation(_map, startPosition, _targetPoints.get(1));
        VehicleBuildTask vehicleBuildTask = new VehicleBuildTask(_vrep, _clientID, _objectCreator, _map, startPosition, orientation, _carmodel);
        vehicleBuildTask.setControlParams(_lookahead, _maxVelocity, _maxLongitudinalAcceleration, _maxLongitudinalDecceleration, _maxLateralAcceleration);
        if(_lowerLayerFactory != null)
        {
            vehicleBuildTask.setLowerLayerFactory(_lowerLayerFactory);
        }
        if(_upperLayerFactory != null)
        {
            vehicleBuildTask.setUpperLayerFactory(_upperLayerFactory);
        }
        _tasks.add(vehicleBuildTask);
        if(!_lowerLayerControls.isEmpty())
        {
            _tasks.add(new AdditionalLowerControlLayerInitTask(_lowerLayerControls, vehicleBuildTask));
        }
        _tasks.add(new SimStartTask(_vrep, _clientID));
        _tasks.add(new VehicleStartTask(vehicleBuildTask));
        if(_debug)
        {
            VehicleStartDebugTask debugTask = new VehicleStartDebugTask(_objectCreator, vehicleBuildTask);
            if(_navigationListeners != null)
            {
                debugTask.addNavigationListeners(_navigationListeners);
            }
            _tasks.add(debugTask);
        }
        Position2D lastTarget = startPosition;
        for(int idx = 0; idx < _targetPoints.size() - 1; idx++)
        {
            Position2D newTarget = _targetPoints.get(idx + 1);
            
//            int timeOutSec = ITaskCreatorConfig.estimateTimeout(lastTarget, newTarget );
            // if we don't make a route in one hour, there is seriously something wrong
            _tasks.add(new DriveAtoBTask(lastTarget, newTarget, 3600, vehicleBuildTask, _map));
        }
        if(_debug)
        {
            _tasks.add(new VehicleStopDebugTask(vehicleBuildTask));
        }
        _tasks.add(new VehicleStopTask(vehicleBuildTask));
        _tasks.add(new SimStopTask(_vrep, _clientID));
        _taskIterator = _tasks.iterator();
    }

    public void setMap(RoadMap map)
    {
        _map = map;
    }

    public void configSimulator(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
    }

    public void addLowerLayerControl(ILowerLayerControl lowerLayerControl)
    {
        _lowerLayerControls.add(lowerLayerControl);
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

    public void addNavigationListener(INavigationListener navigationListener)
    {
        if(_navigationListeners == null)
        {
            _navigationListeners = new ArrayList<>();
        }
        _navigationListeners.add(navigationListener);
    }

    public void setLowerLayerController(ILowerLayerFactory lowerLayerFactory)
    {
        _lowerLayerFactory = lowerLayerFactory;
    }
    
    public void setUpperLayerController(IUpperLayerFactory upperLayerFactory)
    {
        _upperLayerFactory = upperLayerFactory;
    }

    public void setCarModel(String carModel)
    {
        _carmodel = carModel;
    }

    public void setDebug(boolean debug)
    {
        _debug = debug;
    }
}