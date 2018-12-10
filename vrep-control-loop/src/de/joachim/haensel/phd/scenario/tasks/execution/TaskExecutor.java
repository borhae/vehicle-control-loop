package de.joachim.haensel.phd.scenario.tasks.execution;

import java.util.List;
import java.util.concurrent.TimeUnit;

import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.tasks.creation.Task;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.BlockingArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitController;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TaskExecutor
{
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private String _mapFileName;

    public void setMapFileName(String mapFileName)
    {
        _mapFileName = mapFileName;
    }

    public void execute(List<Task> tasks) throws VRepException
    {
        RoadMap map = createMap();
        IVehicle vehicle = createVehicle(map, tasks.get(0).getSource());
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        vehicle.start();

        for (Task curTask : tasks)
        {
            driveSourceToTarget(curTask.getSource(), curTask.getTarget(), map, vehicle);
        }
        
        vehicle.stop();
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
    }

    private RoadMap createMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap(_mapFileName);
        
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        return roadMap;
    }

    private void driveSourceToTarget(Position2D source, Position2D target, RoadMap map, IVehicle vehicle) throws VRepException
    {
        vehicle.setPosition(source.getX(), source.getY(), 2.0);
        BlockingArrivedListener listener = new BlockingArrivedListener(15, TimeUnit.MINUTES);
        vehicle.driveTo(target.getX(), target.getY(), map, listener);
        listener.waitForArrival();
    }
    
    private IVehicle createVehicle(RoadMap map, Position2D vehiclePosition)
    {
        // IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm", 1.0f);
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/carvisuals.ttm", 1.0f);
        IVehicleConfiguration vehicleConf = createConfiguration(map, vehiclePosition);
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        return vehicle;
    }
    
    private IVehicleConfiguration createConfiguration(RoadMap map, Position2D vehiclePosition)
    {
        IVehicleConfiguration vehicleConf = new VRepVehicleConfiguration();
        IUpperLayerFactory upperFact = () -> {return new DefaultNavigationController(5.0, 60.0);};
        ILowerLayerFactory lowerFact = () -> {
            PurePursuitController ctrl = new PurePursuitController();
            PurePursuitParameters parameters = new PurePursuitParameters(10.0, 0.25);
            parameters.setSpeed(2.5);
            ctrl.setParameters(parameters);
            return ctrl;
        };
        vehicleConf.setUpperCtrlFactory(upperFact);
        vehicleConf.setLowerCtrlFactory(lowerFact);
        
        Position2D startingPoint = map.getClosestPointOnMap(vehiclePosition);
        vehicleConf.setPosition(startingPoint.getX(), startingPoint.getY(), 3.0);

        Vector2D firstRoadSection = new Vector2D(map.getClosestLineFor(vehiclePosition));
        vehicleConf.setOrientation(firstRoadSection);
        vehicleConf.setRoadMap(map);
        return vehicleConf;
    }
}
