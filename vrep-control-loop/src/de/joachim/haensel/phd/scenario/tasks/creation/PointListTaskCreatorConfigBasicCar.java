package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.ArrayList;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.tasks.AdditionalLowerControlLayerInitTask;
import de.joachim.haensel.phd.scenario.tasks.DriveAtoBTask;
import de.joachim.haensel.phd.scenario.tasks.SimStartTask;
import de.joachim.haensel.phd.scenario.tasks.SimStopTask;
import de.joachim.haensel.phd.scenario.tasks.SimpleVehicleBuildTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleBuildTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleDeactivateDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartDebugTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStartTask;
import de.joachim.haensel.phd.scenario.tasks.VehicleStopTask;

public class PointListTaskCreatorConfigBasicCar extends PointListTaskCreatorConfig
{

    public PointListTaskCreatorConfigBasicCar(boolean debug)
    {
        super(debug);
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
        SimpleVehicleBuildTask vehicleBuildTask = new SimpleVehicleBuildTask(_vrep, _clientID, _objectCreator, _map, startPosition, orientation);
        _tasks.add(vehicleBuildTask);
        if(!_lowerLayerControls.isEmpty())
        {
            _tasks.add(new AdditionalLowerControlLayerInitTask(_lowerLayerControls, vehicleBuildTask));
        }
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
}
