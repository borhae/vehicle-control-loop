package de.joachim.haensel.phd.scenario.tasks.execution;

import java.util.List;
import java.util.concurrent.TimeUnit;

import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.debug.Speedometer;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.tasks.creation.Task;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.BlockingArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

public class TaskExecutor
{
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private RoadMap _map;

    public TaskExecutor(int clientID, VRepRemoteAPI vrep, VRepObjectCreation objectCreator)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
    }

    public void setMap(RoadMap map)
    {
        _map = map;
    }

    public void execute(List<Task> tasks) throws VRepException
    {
        IVehicle vehicle = createVehicle(_map, tasks.get(0).getSource(), tasks.get(0).getTarget());
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        waitForSimulation(1000);
        vehicle.start();
        
        DebugParams debParam = new DebugParams(); 
        debParam.setSimulationDebugMarkerHeight(2.0);
        Speedometer speedometer = Speedometer.createWindow();
        debParam.setSpeedometer(speedometer);
        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
        debParam.addNavigationListener(navigationListener);
        vehicle.activateDebugging(debParam);

        for (Task curTask : tasks)
        {
            driveSourceToTarget(curTask.getSource(), curTask.getTarget(), _map, vehicle, curTask.getTimeout());
        }
        vehicle.deacvtivateDebugging();
        vehicle.stop();
        waitForSimulation(1000);
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
        waitForSimulation(1000);
    }

    private void driveSourceToTarget(Position2D source, Position2D target, RoadMap map, IVehicle vehicle, int timoutInSeconds) throws VRepException
    {
        BlockingArrivedListener listener = new BlockingArrivedListener(timoutInSeconds, TimeUnit.SECONDS);
        vehicle.driveTo(target.getX(), target.getY(), map, listener);
        listener.waitForArrival();
    }

    private int createTargetLandmark(Position2D target) throws VRepException
    {
        ShapeParameters targetShapeParams = new ShapeParameters();
        targetShapeParams.setIsDynamic(false);
        targetShapeParams.setIsRespondable(false);
        targetShapeParams.setMass(1.0f);
        targetShapeParams.setName("target");
        targetShapeParams.setOrientation(0.0f, 0.0f, 0.0f);
        targetShapeParams.setPosition((float)target.getX(), (float)target.getY(), 1.0f);
        targetShapeParams.setSize(3.0f, 3.0f, 3.0f);
        targetShapeParams.setType(EVRepShapes.CUBOID);
        targetShapeParams.setVisibility(true);
        return _objectCreator.createPrimitive(targetShapeParams);
    }
    
    private IVehicle createVehicle(RoadMap map, Position2D vehiclePosition, Position2D targetPosition)
    {
//        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm", 1.0f);
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/carvisuals.ttm", 1.0f);
        IVehicleConfiguration vehicleConf = SimulationSetupConvenienceMethods.createMercedesLikeConfiguration(map, vehiclePosition, targetPosition, 1.5);
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        return vehicle;
    }

    private void waitForSimulation(int sleepTime)
    {
        try
        {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
    }
}
