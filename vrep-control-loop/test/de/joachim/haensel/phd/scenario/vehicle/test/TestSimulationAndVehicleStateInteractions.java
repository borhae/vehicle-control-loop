package de.joachim.haensel.phd.scenario.vehicle.test;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestSimulationAndVehicleStateInteractions
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
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
    public void testCreateVehicleDriveStop() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Position2D startPosition = new Position2D(5747.01, 2979.22).transform(centerMatrix);
        Position2D destinationPosition = new Position2D(3031.06, 4929.45).transform(centerMatrix);

        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/carvisuals.ttm");
        IVehicleConfiguration vehicleConf = SimulationSetupConvenienceMethods.createMercedesLikeConfiguration(roadMap, startPosition, destinationPosition, 2.0);
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
        debParam.setSimulationDebugMarkerHeight(1.0);
        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
        debParam.addNavigationListener(navigationListener);
        vehicle.activateDebugging(debParam);

        vehicle.start();
        Position2D target = roadMap.getClosestPointOnMap(destinationPosition);
        
        vehicle.driveTo((float)target.getX(), (float)target.getY(), roadMap);

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        
        vehicle.stop();
        vehicle.deacvtivateDebugging();
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
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
    public void testStartSimThenCreateVehicleDriveStop() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Position2D startPosition = new Position2D(5747.01, 2979.22).transform(centerMatrix);
        Position2D destinationPosition = new Position2D(3031.06, 4929.45).transform(centerMatrix);

        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/carvisuals.ttm");
        IVehicleConfiguration vehicleConf = SimulationSetupConvenienceMethods.createMercedesLikeConfiguration(roadMap, startPosition, destinationPosition, 2.0);
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
        DebugParams debParam = new DebugParams(); 
        debParam.setSimulationDebugMarkerHeight(1.0);
        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
        debParam.addNavigationListener(navigationListener);
        vehicle.activateDebugging(debParam);

        vehicle.start();
        Position2D target = roadMap.getClosestPointOnMap(destinationPosition);
        
        vehicle.driveTo((float)target.getX(), (float)target.getY(), roadMap);

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        
        vehicle.stop();
        vehicle.deacvtivateDebugging();
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
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
}
