package de.joachim.haensel.phd.scenario.vehicle.experiment.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.debug.Speedometer;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfigBasicCar;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitController;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TireBlowOutAfterDistanceEventGenerator;
import de.joachim.haensel.phd.scenario.vehicle.experiment.TireBlowOutAtPositionEventGenerator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestTireBlowout implements TestConstants
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
    public void testTireBlowOutScenario1() throws VRepException
    {
        double scaleFactor = 1.0;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix scaleOffsetMatrix = roadMap.center(0.0, 0.0);
        
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        Position2D startPosition = new Position2D(5842.68,3007.94).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(5527.21,3391.06).transform(scaleOffsetMatrix);

        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm", 1.0f);
        IVehicleConfiguration vehicleConf = createConfiguration(roadMap, startPosition, destinationPosition);
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        vehicle.addLowLevelEventGeneratorListener(new TireBlowOutAfterDistanceEventGenerator(100.0, 0.5f));

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
    public void testTireBlowOutScenario2Variant1()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            config.setControlParams(20, 120, 2.0, 3.0, 1.3);

            Position2D p2 = new Position2D(3122.84, 4937.96).transform(centerMatrix);
            Position2D p3 = new Position2D(2998.93, 4829.77).transform(centerMatrix);
            Position2D p4 = new Position2D(3246.30, 2117.18).transform(centerMatrix);

            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            config.addLowerLayerControl(new TireBlowOutAfterDistanceEventGenerator(100.0, 0.5f));

            config.setTargetPoints(Arrays.asList(new Position2D[] { p2, p3, p4 }));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
        	fail(exc);
        }
    }
    
    @Test
    public void testTireBlowOutScenario2Variant2()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            config.setControlParams(20, 120, 6.0, 8.0, 5.0);

            Position2D p1 = new Position2D(3653.19, 4666.35).transform(centerMatrix);
            Position2D p2 = new Position2D(3845.60, 4744.58).transform(centerMatrix);
            Position2D p3 = new Position2D(3599.51, 4841.12).transform(centerMatrix);
            
            Position2D blowoutPosition = new Position2D(3861.07, 4705.83).transform(centerMatrix);

            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            config.addLowerLayerControl(new TireBlowOutAtPositionEventGenerator(blowoutPosition, 10.0, new boolean[]{true, false, false, false}, 0.5f));

            config.setTargetPoints(Arrays.asList(new Position2D[] { p1, p2, p3 }));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }
    
    @Test
    public void test2Variant2NoBlowout()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfig(true);
            config.setControlParams(20, 120, 6.0, 8.0, 5.0);

            Position2D p1 = new Position2D(3653.19, 4666.35).transform(centerMatrix);
            Position2D p2 = new Position2D(3845.60, 4744.58).transform(centerMatrix);
            Position2D p3 = new Position2D(3599.51, 4841.12).transform(centerMatrix);
            

            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);

            config.setTargetPoints(Arrays.asList(new Position2D[] { p1, p2, p3 }));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }
    
    @Test
    public void testTireBlowOutScenario3()
    {
        try
        {
            RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
            RoadMap map = mapAndCenterMatrix.getRoadMap();
            TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig config = new PointListTaskCreatorConfigBasicCar(true);
            
            Position2D p2 = new Position2D(2967.31, 4819.69).transform(centerMatrix);
            Position2D p3 = new Position2D(2924.78, 4830.75).transform(centerMatrix);
            Position2D p4 = new Position2D(2947.13, 4796.47).transform(centerMatrix);

            config.setMap(map);
            config.configSimulator(_vrep, _clientID, _objectCreator);
            config.addLowerLayerControl(new TireBlowOutAfterDistanceEventGenerator(120, new float[]{1.2f, 0.2f}));

            config.setTargetPoints(Arrays.asList(new Position2D[] { p2, p3, p4 }));
            taskCreator.configure(config);
            List<ITask> tasks = taskCreator.createTasks();

            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
            System.out.println("bla");
        }
        catch (VRepException exc)
        {
            fail(exc);
        }
    }

    private IVehicleConfiguration createConfiguration(RoadMap roadMap, Position2D startPosition, Position2D destinationPosition)
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
        
        Position2D startingPoint = roadMap.getClosestPointOnMap(startPosition);
        vehicleConf.setPosition(startingPoint.getX(), startingPoint.getY(), 3.0);

        Vector2D firstRoadSection = new Vector2D(roadMap.getClosestLineFor(startPosition));
        vehicleConf.setOrientation(firstRoadSection);
        vehicleConf.setRoadMap(roadMap);
        return vehicleConf;
    }
}
