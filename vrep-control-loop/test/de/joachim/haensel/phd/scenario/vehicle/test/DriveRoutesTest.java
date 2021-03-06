package de.joachim.haensel.phd.scenario.vehicle.test;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.debug.Speedometer;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitVariableLookaheadController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class DriveRoutesTest
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    @AfterAll
    public static void tearDownVrep() throws VRepException 
    {
        waitForRunningSimulationToStop();
        _vrep.simxFinish(_clientID);
    }

    private static void waitForRunningSimulationToStop() throws VRepException
    {
        IntWA simStatus = new IntWA(1);
        _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
        while(simStatus.getArray()[0] != remoteApi.sim_simulation_stopped)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException exc)
            {
                exc.printStackTrace();
            }
            _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
        }
    }

    @AfterEach
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }
    
    @Test
    public void testRouteFollowRealMapMesh() throws VRepException
    {
        double scaleFactor = 1.0;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix scaleOffsetMatrix = roadMap.center(0.0, 0.0);
        
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);

//        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm", 1.0f);
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/carvisuals.ttm");
        IVehicleConfiguration vehicleConf = createConfiguration(roadMap, startPosition, destinationPosition);
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        DebugParams debParam = new DebugParams(); 
        debParam.setSimulationDebugMarkerHeight(scaleFactor);
        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
//        navigationListener.activateRouteDebugging();
        debParam.addNavigationListener(navigationListener);
        Speedometer speedometer = Speedometer.createWindow();
        debParam.setSpeedometer(speedometer);
        vehicle.activateDebugging(debParam);

        vehicle.start();
        Position2D target = roadMap.getClosestPointOnMap(destinationPosition);
        
        vehicle.driveTo((float)target.getX(), (float)target.getY(), roadMap);
        
        System.out.println("wait here");
        vehicle.stop();
        vehicle.deacvtivateDebugging();
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
    }

    @Test
    public void testSpeedSuddenStopOnRealMap() throws VRepException
    {
        double scaleFactor = 1.0;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix scaleOffsetMatrix = roadMap.center(0.0, 0.0);
        
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);

        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm");
        IVehicleConfiguration vehicleConf = createConfiguration(roadMap, startPosition, destinationPosition);
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        DebugParams debParam = new DebugParams(); 
        debParam.setSimulationDebugMarkerHeight(scaleFactor);
        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
//        navigationListener.activateRouteDebugging();
        debParam.addNavigationListener(navigationListener);
        Speedometer speedometer = Speedometer.createWindow();
        debParam.setSpeedometer(speedometer);
        vehicle.activateDebugging(debParam);

        vehicle.start();
        Position2D target = roadMap.getClosestPointOnMap(destinationPosition);
        
        vehicle.driveTo((float)target.getX(), (float)target.getY(), roadMap);
        
        System.out.println("wait here");
        vehicle.stop();
        vehicle.deacvtivateDebugging();
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
    }
    
    private IVehicleConfiguration createConfiguration(RoadMap roadMap, Position2D startPosition, Position2D destinationPosition)
    {
        IVehicleConfiguration vehicleConf = new VRepVehicleConfiguration();
        IUpperLayerFactory upperFact = () -> {return new DefaultNavigationController(5.0, 60.0);};
        ILowerLayerFactory lowerFact = () -> {
            PurePursuitVariableLookaheadController ctrl = new PurePursuitVariableLookaheadController();
            return ctrl;
        };
        vehicleConf.setUpperCtrlFactory(upperFact);
        vehicleConf.setLowerCtrlFactory(lowerFact);
        
        Position2D startingPoint = roadMap.getClosestPointOnMap(startPosition);
        vehicleConf.setPosition(startingPoint.getX(), startingPoint.getY(), 3.0);

        Vector2D firstRoadSection = new Vector2D(roadMap.getClosestLineFor(startPosition));
        vehicleConf.setOrientation(firstRoadSection);
        vehicleConf.setRoadMap(roadMap);
        return vehicleConf;
    }
}
