package de.joachim.haensel.phd.scenario.vehicle.test;


import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.Speedometer;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.simulator.vrep.VRepSimulatorAndVehicleData;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.NullBehaviorActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.Vehicle;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable.AtomicSetActualError;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitVariableLookaheadController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepPartwiseVehicleCreator;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleActuatorsSensors;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class LayerInteractionTest implements TestConstants
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeEach
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
    public void testNavigationController()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        DefaultNavigationController controller = new DefaultNavigationController(2.0, 30.0);
        Position2D destinationPosition = new Position2D(101.81f, 9.23f);
        IActuatingSensing sensorsActuators = new NullBehaviorActuatingSensing() 
        {
            @Override
            public Position2D getPosition()
            {
                return new Position2D(0.73, 39.18);
            }
        };
        controller.initController(sensorsActuators, roadMap);
        controller.buildSegmentBuffer(destinationPosition, roadMap);
        System.out.println("done");
    }
    
    @Test
    public void testRouteFollow3JunctionMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0, 0.0);
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(11.4f, 101.4f);
        Position2D destinationPosition = new Position2D(101.81f, 9.23f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, DOWN_SCALE_FACTOR);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        
        IUpperLayerFactory uperFact = () -> {return new DefaultNavigationController(2.0, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitVariableLookaheadController();};
        
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, uperFact , lowerFact);
        
        correctVehicleOrientation(DOWN_SCALE_FACTOR, roadMap, destinationPosition, vehicle);
        
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
        debParam.setSimulationDebugMarkerHeight(1.0 * DOWN_SCALE_FACTOR);
        vehicle.activateDebugging(debParam);
        vehicle.start();
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
    public void testRouteFollowRealMapNoVisualization() throws VRepException
    {
        float scale = 1.0f;
        double segmentSize = 2.0;
        double lookahead = 10.0;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        
        XYMinMax dimensions = roadMap.computeMapDimensions();
        double offX = dimensions.minX() + dimensions.distX()/2.0;
        double offY = dimensions.minY() + dimensions.distY()/2.0;
        TMatrix scaleOffsetMatrix = new TMatrix(scale, -offX, -offY);
        roadMap.transform(scaleOffsetMatrix);
        
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangle(roadMap, true);
        
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, scale);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        IUpperLayerFactory uperFact = () -> {return new DefaultNavigationController(segmentSize, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitVariableLookaheadController();};
        
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, uperFact , lowerFact);
        
        correctVehicleOrientation(scale, roadMap, destinationPosition, vehicle);
        
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
        debParam.setSimulationDebugMarkerHeight(DOWN_SCALE_FACTOR);
        vehicle.activateDebugging(debParam);

        vehicle.start();
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
    
    // Does not work, simulation too slow. Creating simple shapes is just too many objects to simulate
    @Test
    public void testRouteFollowRealMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, DOWN_SCALE_FACTOR);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        
        IUpperLayerFactory uperFact = () -> {return new DefaultNavigationController(2.0, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitVariableLookaheadController();};
        
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, uperFact , lowerFact);
        
        correctVehicleOrientation(1.0, roadMap, destinationPosition, vehicle);
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
        debParam.setSimulationDebugMarkerHeight(1.0 * DOWN_SCALE_FACTOR);
        vehicle.activateDebugging(debParam);

        vehicle.start();
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

    //Kind of works, creates huge texture with bad resolution. dropped it too
    @Test
    public void testRouteFollowRealMapTexture() throws VRepException
    {
        double scaleFactor = 1.0;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        XYMinMax dimensions = roadMap.computeMapDimensions();
        double offX = dimensions.minX() + dimensions.distX()/2.0;
        double offY = dimensions.minY() + dimensions.distY()/2.0;
        offX *= scaleFactor;
        offY *= scaleFactor;

        TMatrix scaleOffsetMatrix = new TMatrix(scaleFactor, -offX, -offY);
        roadMap.transform(scaleOffsetMatrix);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangleWithMapTexture(roadMap);
        
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, (float)scaleFactor);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX2(), lastLine.getY2());
        
        
        IUpperLayerFactory uperFact = () -> {return new DefaultNavigationController(2.0 * scaleFactor, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitVariableLookaheadController();};
        
        float vehicleZPos = 0.25f;
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), vehicleZPos, roadMap, uperFact , lowerFact);
        
        correctVehicleOrientation(scaleFactor, roadMap, destinationPosition, vehicle);
        
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
        vehicle.activateDebugging(debParam);

        vehicle.start();
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

    //TODO this test is not really working. I fixed it temporarily by adding PI to the correction angle
    @Test
    public void testRouteFollowRealMapMesh() throws VRepException
    {
        double scaleFactor = 1.0;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        XYMinMax dimensions = roadMap.computeMapDimensions();
        double offX = dimensions.minX() + dimensions.distX()/2.0;
        double offY = dimensions.minY() + dimensions.distY()/2.0;
        offX *= scaleFactor;
        offY *= scaleFactor;

        TMatrix scaleOffsetMatrix = new TMatrix(scaleFactor, -offX, -offY);
        roadMap.transform(scaleOffsetMatrix);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, (float)scaleFactor);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX2(), lastLine.getY2());
        
        IUpperLayerFactory uperFact = () -> {return new DefaultNavigationController(5.0 * scaleFactor, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitVariableLookaheadController();};
        
        float vehicleZPos = 0.25f;
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), vehicleZPos, roadMap, uperFact , lowerFact);
        
        correctVehicleOrientation(scaleFactor, roadMap, destinationPosition, vehicle);
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
        Speedometer speedometer = Speedometer.createWindow();
        debParam.setSpeedometer(speedometer);
        vehicle.activateDebugging(debParam);

        vehicle.start();
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
    public void testRoute2FollowRealMapMesh() throws VRepException
    {
        double scaleFactor = 1.0;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        XYMinMax dimensions = roadMap.computeMapDimensions();
        double offX = dimensions.minX() + dimensions.distX()/2.0;
        double offY = dimensions.minY() + dimensions.distY()/2.0;
        offX *= scaleFactor;
        offY *= scaleFactor;

        TMatrix scaleOffsetMatrix = new TMatrix(scaleFactor, -offX, -offY);
        roadMap.transform(scaleOffsetMatrix);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(1450.3, 751.9);
        Position2D destinationPosition = new Position2D(-1737.6252, 1488.9);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, (float)scaleFactor);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX2(), lastLine.getY2());
        
        IUpperLayerFactory uperFact = () -> {return new DefaultNavigationController(4.0 * scaleFactor, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitVariableLookaheadController();};
        
        float vehicleZPos = 0.25f;
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), vehicleZPos, roadMap, uperFact , lowerFact);
        
        correctVehicleOrientation(scaleFactor, roadMap, destinationPosition, vehicle);

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
        vehicle.activateDebugging(debParam);

        vehicle.start();
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

    //TODO find out about cases were we have an exact 180 degree error
    private void correctVehicleOrientation(double scaleFactor, RoadMap roadMap, Position2D destinationPosition, Vehicle vehicle) throws VRepException
    {
        Vector2D carOrientation = vehicle.getOrientation();
        DefaultNavigationController fakeNav = new DefaultNavigationController(2.0 *  scaleFactor, 30.0);
        VRepSimulatorAndVehicleData simData = new VRepSimulatorAndVehicleData(null, _vrep, _clientID, null);
        fakeNav.initController(new VRepVehicleActuatorsSensors(vehicle.getVehicleHandles(), simData, roadMap), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);

        int elementsToRequest = fakeNav.getSegmentBufferSize();
        Deque<Vector2D> input = fakeNav.getNewElements(elementsToRequest, AtomicSetActualError.INITIALIZATION_REQUEST).stream().map(traj -> traj.getVector()).collect(Collectors.toCollection(LinkedList::new));
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        visualizer.addVectorSet(input, Color.BLUE);
        visualizer.updateVisuals();
        visualizer.setVisible(true);
        
        TrajectoryElement firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation);
        
        vehicle.setOrientation(0.0f, 0.0f, (float)(correctionAngle * Math.PI));
    }
}
