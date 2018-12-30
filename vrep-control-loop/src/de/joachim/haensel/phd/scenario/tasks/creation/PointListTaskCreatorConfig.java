package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.tasks.DriveAtoBTask;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.SimStartTask;
import de.joachim.haensel.phd.scenario.tasks.SimStopTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleBuildTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleDeactivateDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStopTask;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class PointListTaskCreatorConfig implements ITaskCreatorConfig, IDrivingTask
{
    private List<Position2D> _targetPoints;
    private List<ITask> _tasks;
    private RoadMap _map;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private boolean _debug;
    private Iterator<ITask> _taskIterator;

    public PointListTaskCreatorConfig(boolean debug)
    {
        _targetPoints = new ArrayList<>();
        _debug = debug;
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
        VehicleBuildTask vehicleBuildTask = new VehicleBuildTask(_vrep, _clientID, _objectCreator, _map, startPosition, orientation);
        _tasks.add(vehicleBuildTask);
        _tasks.add(new SimStartTask(_vrep, _clientID));
        _tasks.add(new VehicleStartTask(vehicleBuildTask));
        if(_debug)
        {
            _tasks.add(new VehicleStartDebugTask(_objectCreator, vehicleBuildTask));
        }
        Position2D lastTarget = startPosition;
        for(int idx = 0; idx < _targetPoints.size() - 1; idx++)
        {
            Position2D newTarget = _targetPoints.get(idx + 1);
            int timeOutSec = ITaskCreatorConfig.estimateTimeout(lastTarget, newTarget );
            _tasks.add(new DriveAtoBTask(lastTarget, newTarget, timeOutSec , vehicleBuildTask, _map));
        }
        if(_debug)
        {
            _tasks.add(new VehicleDeactivateDebugTask(vehicleBuildTask));
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
}    