package de.joachim.haensel.phd.scenario.vehicle.test;

import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.sumo2vrep.VRepMap;
import de.joachim.haensel.sumo2vrep.XYMinMax;
import de.joachim.haensel.vehicle.BadReactiveController;
import de.joachim.haensel.vehicle.IActuatingSensing;
import de.joachim.haensel.vehicle.ILowerLayerFactory;
import de.joachim.haensel.vehicle.IUpperLayerFactory;
import de.joachim.haensel.vehicle.NavigationController;
import de.joachim.haensel.vehicle.PurePursuitParameters;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleActuatorsSensors;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vehiclecontrol.Navigator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class LayerInteractionTest implements TestConstants
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeClass
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    @AfterClass
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

    @After
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }
    
    @Test
    public void testNavigationController()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        NavigationController controller = new NavigationController(2.0);
        Position2D destinationPosition = new Position2D(101.81f, 9.23f);
        IActuatingSensing sensorsActuators = new IActuatingSensing() {
            @Override
            public double getVehicleLength()
            {
                return 0;
            }
            
            @Override
            public Position2D getRearWheelCenterPosition()
            {
                return null;
            }
            
            @Override
            public Position2D getFrontWheelCenterPosition()
            {
                return null;
            }
            
            @Override
            public Position2D getPosition()
            {
                return new Position2D(0.73, 39.18);
            }
            
            @Override
            public void drive(float targetWheelRotation, float targetSteeringAngle){ }

            @Override
            public void computeAndLockSensorData()
            {
            }

            @Override
            public void setOrientation(float angleAlpha, float angleBeta, float angleGamma)
            {
            }

            @Override
            public void setPosition(float posX, float posY, float posZ)
            {
            }

            @Override
            public Vector2D getOrientation()
            {
                return null;
            }

            @Override
            public Position2D getNonDynamicPosition()
            {
                return null;
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
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, DOWN_SCALE_FACTOR);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        
        IUpperLayerFactory uperFact = () -> {return new NavigationController(2.0);};
        BadReactiveController ctrl = new BadReactiveController(); 
        ctrl.setParameters(new PurePursuitParameters(5.0 * DOWN_SCALE_FACTOR));
        ILowerLayerFactory lowerFact = () -> {return ctrl;};
        
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, uperFact , lowerFact);
        
        Vector2D carOrientation = vehicle.getOrientation();
        NavigationController fakeNav = new NavigationController(2.0);
        fakeNav.initController(new VehicleActuatorsSensors(vehicle.getVehicleHandles(), vehicle.getController(), _vrep, _clientID), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);
        
        Trajectory firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation) + Math.PI;
        
        vehicle.setOrientation(0.0f, 0.0f, (float)correctionAngle);

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
        vehicle.activateDebugging(1.0 * DOWN_SCALE_FACTOR);
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
        mapCreator.createMapSizedRectangle(roadMap);
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, scale);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        IUpperLayerFactory uperFact = () -> {return new NavigationController(segmentSize);};
        BadReactiveController ctrl = new BadReactiveController();
        PurePursuitParameters reactiveControllerParameters = new PurePursuitParameters(lookahead);
        reactiveControllerParameters.setSpeed(lookahead);
        ctrl.setParameters(reactiveControllerParameters);
        ILowerLayerFactory lowerFact = () -> {return ctrl;};
        
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, uperFact , lowerFact);
        
        Vector2D carOrientation = vehicle.getOrientation();
        NavigationController fakeNav = new NavigationController(segmentSize);
        fakeNav.initController(new VehicleActuatorsSensors(vehicle.getVehicleHandles(), vehicle.getController(), _vrep, _clientID), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);
        
        Trajectory firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation) + Math.PI;
        
        vehicle.setOrientation(0.0f, 0.0f, (float)correctionAngle);

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
        vehicle.activateDebugging(DOWN_SCALE_FACTOR);
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
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, DOWN_SCALE_FACTOR);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        
        IUpperLayerFactory uperFact = () -> {return new NavigationController(2.0);};
        ILowerLayerFactory lowerFact = () -> {return new BadReactiveController();};
        
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, uperFact , lowerFact);
        
        Vector2D carOrientation = vehicle.getOrientation();
        NavigationController fakeNav = new NavigationController(2.0);
        fakeNav.initController(new VehicleActuatorsSensors(vehicle.getVehicleHandles(), vehicle.getController(), _vrep, _clientID), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);
        
        Trajectory firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation) + Math.PI;
        
        vehicle.setOrientation(0.0f, 0.0f, (float)correctionAngle);

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
        vehicle.activateDebugging(DOWN_SCALE_FACTOR);
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
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, (float)scaleFactor);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX2(), lastLine.getY2());
        
        
        IUpperLayerFactory uperFact = () -> {return new NavigationController(2.0 * scaleFactor);};
        BadReactiveController ctrl = new BadReactiveController(); 
        ctrl.setParameters(new PurePursuitParameters(5.0 * scaleFactor));
        ILowerLayerFactory lowerFact = () -> {return ctrl;};
        
        float vehicleZPos = 0.25f;
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), vehicleZPos, roadMap, uperFact , lowerFact);
        
        Vector2D carOrientation = vehicle.getOrientation();
        NavigationController fakeNav = new NavigationController(2.0 *  scaleFactor);
        fakeNav.initController(new VehicleActuatorsSensors(vehicle.getVehicleHandles(), vehicle.getController(), _vrep, _clientID), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);

        Deque<Vector2D> input = fakeNav.getNewSegments(fakeNav.getSegmentBufferSize()).stream().map(traj -> traj.getVector()).collect(Collectors.toCollection(LinkedList::new));
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        visualizer.addVectorSet(input, Color.BLUE);
        visualizer.updateVisuals();
        visualizer.setVisible(true);
        System.out.println("stop");
        
        Trajectory firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation) + Math.PI;
        
        vehicle.setOrientation(0.0f, 0.0f, (float)correctionAngle);

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
        vehicle.activateDebugging(scaleFactor);
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
        mapCreator.createMapSizedRectangle(roadMap);
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, (float)scaleFactor);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX2(), lastLine.getY2());
        
        
        IUpperLayerFactory uperFact = () -> {return new NavigationController(4.0 * scaleFactor);};
        BadReactiveController ctrl = new BadReactiveController(); 
        PurePursuitParameters parameters = new PurePursuitParameters(10.0 * scaleFactor);
        parameters.setSpeed(1.5);
        ctrl.setParameters(parameters);
        ILowerLayerFactory lowerFact = () -> {return ctrl;};
        
        float vehicleZPos = 0.25f;
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), vehicleZPos, roadMap, uperFact , lowerFact);
        
        Vector2D carOrientation = vehicle.getOrientation();
        NavigationController fakeNav = new NavigationController(2.0 *  scaleFactor);
        fakeNav.initController(new VehicleActuatorsSensors(vehicle.getVehicleHandles(), vehicle.getController(), _vrep, _clientID), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);

        Deque<Vector2D> input = fakeNav.getNewSegments(fakeNav.getSegmentBufferSize()).stream().map(traj -> traj.getVector()).collect(Collectors.toCollection(LinkedList::new));
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        visualizer.addVectorSet(input, Color.BLUE);
        visualizer.updateVisuals();
        visualizer.setVisible(true);
        System.out.println("stop");
        
        Trajectory firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation) + Math.PI;
        
        vehicle.setOrientation(0.0f, 0.0f, (float)correctionAngle);

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
        vehicle.activateDebugging(scaleFactor);
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
    public void testRouteFollowRealMapScaledDown() throws VRepException
    {
        float scaleFactor = 0.1f;
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
        VRepMap mapCreator = new VRepMap(STREET_WIDTH * scaleFactor, STREET_HEIGHT * scaleFactor, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangle(roadMap);
//        mapCreator.createMap(roadMap);
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, scaleFactor);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        
        IUpperLayerFactory uperFact = () -> {return new NavigationController(2.0 * scaleFactor);};
        BadReactiveController ctrl = new BadReactiveController(); 
        ctrl.setParameters(new PurePursuitParameters(5.0 * scaleFactor));
        ILowerLayerFactory lowerFact = () -> {return ctrl;};
        
//        float vehicleZPos = 0.0f + vehicleCreator.getVehicleHeight() + 0.2f;
        float vehicleZPos = 0.5f;
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), vehicleZPos, roadMap, uperFact , lowerFact);
        
        Vector2D carOrientation = vehicle.getOrientation();
        NavigationController fakeNav = new NavigationController(2.0 *  scaleFactor);
        fakeNav.initController(new VehicleActuatorsSensors(vehicle.getVehicleHandles(), vehicle.getController(), _vrep, _clientID), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);
        
        Trajectory firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation) + Math.PI;
        
        vehicle.setOrientation(0.0f, 0.0f, (float)correctionAngle);

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
        vehicle.activateDebugging(scaleFactor);
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
    public void testRouteFollowSimpleMapScaledDown() throws VRepException
    {
        float scaleFactor = 0.1f;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        XYMinMax dimensions = roadMap.computeMapDimensions();
        double offX = dimensions.minX() + dimensions.distX()/2.0;
        double offY = dimensions.minY() + dimensions.distY()/2.0;
        offX *= scaleFactor;
        offY *= scaleFactor;

        TMatrix scaleOffsetMatrix = new TMatrix(scaleFactor, -offX, -offY);
        roadMap.transform(scaleOffsetMatrix);
        
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(-5.0f, 5.0f);
        Position2D destinationPosition = new Position2D(5.0f, -5.0f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH * scaleFactor, STREET_HEIGHT * scaleFactor, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, scaleFactor);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX2(), lastLine.getY2());
        
        
        IUpperLayerFactory uperFact = () -> {return new NavigationController(2.0 * scaleFactor);};
        BadReactiveController ctrl = new BadReactiveController(); 
        ctrl.setParameters(new PurePursuitParameters(5.0 * scaleFactor));
        ILowerLayerFactory lowerFact = () -> {return ctrl;};
        
        float vehicleZPos = 0.25f;
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), vehicleZPos, roadMap, uperFact , lowerFact);
        
        Vector2D carOrientation = vehicle.getOrientation();
        NavigationController fakeNav = new NavigationController(2.0 *  scaleFactor);
        fakeNav.initController(new VehicleActuatorsSensors(vehicle.getVehicleHandles(), vehicle.getController(), _vrep, _clientID), roadMap);
        fakeNav.buildSegmentBuffer(destinationPosition, roadMap);

        Deque<Vector2D> input = fakeNav.getNewSegments(fakeNav.getSegmentBufferSize()).stream().map(traj -> traj.getVector()).collect(Collectors.toCollection(LinkedList::new));
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        visualizer.addVectorSet(input, Color.BLUE);
        visualizer.updateVisuals();
        visualizer.setVisible(true);
        System.out.println("stop");
        
        Trajectory firstSeg = fakeNav.segmentsPeek();
        Vector2D firstSegOrientation = firstSeg.getVector();
        
        double correctionAngle = Vector2D.computeAngle(carOrientation, firstSegOrientation) + Math.PI;
        
        vehicle.setOrientation(0.0f, 0.0f, (float)correctionAngle);

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
        vehicle.activateDebugging(scaleFactor);
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
}
