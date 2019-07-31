package de.joachim.haensel.phd.scenario.experiment.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener.IIDCreator;
import de.joachim.haensel.phd.scenario.experiment.setup.ValidatedRoadPositionGenerator;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.simulator.vrep.VRepSimulatorData;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class ExperimentGeneratorAndRunner
{
    public static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";

    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    public static void main(String[] args)
    {
        try
        {
            ValidatedRoadPositionGenerator positionGenerator = new ValidatedRoadPositionGenerator();
            boolean positionFileSaved = positionGenerator.runUserInteraction(false);
            if (positionFileSaved)
            {
                ExperimentGeneratorAndRunner runner = new ExperimentGeneratorAndRunner();
                if (positionGenerator.simulatorConnected())
                {
                    runner.initialize(positionGenerator.getSimulatorData());
                } 
                else
                {
                    runner.initialize();
                }
                System.out.println("Start experiment? y and <enter> to start, anything else and <enter> to leave.");
                Scanner scanner = new Scanner(System.in);
                String answer = scanner.next();
                if(answer.equalsIgnoreCase("y"))
                {
                    try
                    {
                        String positionFileName = positionGenerator.getResultPath();
                        
                        List<String> pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + positionFileName).toPath());
                        List<Position2D> allPositions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());
                        List<Position2D> positions = allPositions.subList(0, allPositions.size());
                        
                        String mapFilenName = positionGenerator.getMapFileName();
                        String testID = String.format("%s_%d_gen_S%d_", positionGenerator.getCityName(), positionGenerator.getNumberOfPositions(), positionGenerator.getSeed()); 
                        runner.run(testID, 15.0, 120.0, 3.8, 4.0, 0.8, positions, mapFilenName, "blue", 120);
                        
                        runner.tearDown();
                    } catch (IOException exc)
                    {
                        exc.printStackTrace();
                    }
                }
                scanner.close();
            }
        } 
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }

    }

    private void initialize(VRepSimulatorData simulatorData)
    {
        _vrep = simulatorData.getVrepAPI();
        _clientID = simulatorData.getClientID();
        _objectCreator = simulatorData.getObjectCreator();
    }
    
    public void initialize() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    private static void waitForRunningSimulationToStop() throws VRepException
    {
        IntWA simStatus = new IntWA(1);
        _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript,
                "simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
        while (simStatus.getArray()[0] != remoteApi.sim_simulation_stopped)
        {
            try
            {
                Thread.sleep(500);
            } catch (InterruptedException exc)
            {
                exc.printStackTrace();
            }
            _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript,
                    "simulationState", null, null, null, null, simStatus, null, null, null,
                    remoteApi.simx_opmode_blocking);
        }
    }

    public void run(String testID, double lookahead, double maxVelocity, double maxLongitudinalAcceleration,
            double maxLongitudinalDecceleration, double maxLateralAcceleration, List<Position2D> targetPoints,
            String mapFilenName, String color, int controlLoopRate) throws VRepException
    {
        RoadMap map = null;
        List<Position2D> targetPointsMapped = null;
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID,
                _vrep, _objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
        String finalTestID;
        map = mapAndCenterMatrix.getRoadMap();
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        targetPointsMapped = targetPoints.stream().map(point -> point.transform(centerMatrix))
                .collect(Collectors.toList());
        finalTestID = testID + String.format("%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity,
                maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);

        TaskCreator taskCreator = new TaskCreator();
        PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
        taskConfiguration.setControlParams(lookahead, maxVelocity, maxLongitudinalAcceleration,
                maxLongitudinalDecceleration, maxLateralAcceleration);
        taskConfiguration.setControlLoopRate(controlLoopRate);
        taskConfiguration.setDebug(true);
        taskConfiguration.setMap(map);
        taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);

        String baseOutputDirectory = "./res/operationalprofiletest/serializedruns/";
        RegularSavingTrajectoryRecorder trajectoryRecorder = new RegularSavingTrajectoryRecorder(50, 100,
                baseOutputDirectory, finalTestID);
        RegularSavingReportListener reportListener = new RegularSavingReportListener(100, baseOutputDirectory,
                finalTestID);
        RegularSavingRequestListener requestListener = new RegularSavingRequestListener(100, baseOutputDirectory,
                finalTestID);

        taskConfiguration.addLowerLayerControl(trajectoryRecorder);
        taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisualsBrakeScript.ttm");

        taskConfiguration.setTargetPoints(targetPointsMapped);
        taskConfiguration.addNavigationListener(trajectoryRecorder);
        taskConfiguration.setLowerLayerController(new ILowerLayerFactory()
        {
            @Override
            public ILowerLayerControl create()
            {
                PurePursuitControllerVariableLookahead controller = new PurePursuitControllerVariableLookahead();
                controller.addTrajectoryRequestListener(requestListener);
                controller.addTrajectoryReportListener(reportListener);
                return controller;
            }
        });

        IIDCreator routeIdCreator = new RouteIDCreator(0);
        VRepNavigationListener routesEndsMarker = new VRepNavigationListener(_objectCreator, routeIdCreator);
        routesEndsMarker.activateRouteEndsDebugging();
        taskConfiguration.addNavigationListener(routesEndsMarker);

        taskCreator.configure(taskConfiguration);
        List<ITask> tasks = taskCreator.createTasks();

        TaskExecutor executor = new TaskExecutor();
        executor.execute(tasks);

        System.out.println("Executed all tasks, now final serialization of results");
        trajectoryRecorder.savePermanently();
        reportListener.savePermanently();
        requestListener.savePermanently();
        System.out.println("Serialization finished. WAIT FOR THIS!");
    }

    public void tearDown() throws VRepException
    {
        _objectCreator.deleteAll();
        waitForRunningSimulationToStop();
        _objectCreator.removeScriptloader();
        _vrep.simxFinish(_clientID);
    }
}
