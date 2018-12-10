package de.joachim.haensel.phd.scenario.map.sumo2vrep.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.OrientedPosition;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.Vehicle;
import de.joachim.haensel.phd.scenario.vehicle.control.BlockingArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitController;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.test.VehicleTestConvenienceSetupMethods;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepPartwiseVehicleCreator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class SubScenarioCreationTest implements TestConstants
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
        _vrep.simxFinish(_clientID);
    }

    @After
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
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
        
        IUpperLayerFactory upperFact = () -> {return new DefaultNavigationController(2.0, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitController();};

        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, upperFact, lowerFact);
        vehicle.setOrientation(0.0f, 0.0f, 0.0f);
        
        vehicle.driveTo((float)target.getX(), (float)target.getY(), roadMap);
    }
    
    @Test
    public void testSetVehicleOnSimpleRoadNetwork() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0, 0.0);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, DOWN_SCALE_FACTOR);
        float height = vehicleCreator.getVehicleHeight();

        IUpperLayerFactory upperFact = () -> {return new DefaultNavigationController(2.0, 30.0);};
        ILowerLayerFactory lowerFact = () -> {return new PurePursuitController();};

        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f, roadMap, upperFact, lowerFact);
        vehicle.setOrientation(0.0f, 0.0f, 0.0f);
        vehicle.setPosition(3.0f, 2.0f, 1.0f);
    }
    
    @Test
    public void testShortDrive() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0, 0.0);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        VRepLoadModelVehicleFactory vehicleCreator = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm", DOWN_SCALE_FACTOR);

        JunctionType startingJunction = roadMap.getJunctions().get(3);
        String startingLaneID = startingJunction.getIncLanes().split(" ")[0];
        LaneType lane = roadMap.getLaneForName(startingLaneID);
        OrientedPosition startPos = roadMap.computeLaneEntryAtJunction(startingJunction, lane);

        JunctionType targetJunction = roadMap.getJunctions().get(2);
        OrientedPosition targetPoint = roadMap.computeLaneEntryAtJunction(targetJunction, lane);
        
        IVehicleConfiguration vehicleConf = VehicleTestConvenienceSetupMethods.createConfiguration(roadMap, startPos.getPos(), 1.5);
        vehicleCreator.configure(vehicleConf);
        IVehicle vehicle = vehicleCreator.createVehicleInstance();
        
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        
        vehicle.start();
        BlockingArrivedListener listener = new BlockingArrivedListener(30, TimeUnit.SECONDS);
        vehicle.driveTo(targetPoint.getPos().getX(), targetPoint.getPos().getY(), roadMap, listener);
        listener.waitForArrival();
        vehicle.stop();
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
        // need to wait some time before call to simxfinish, otherwise vrep crashes -> weird 
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
