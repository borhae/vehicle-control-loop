package de.joachim.haensel.phd.scenario.vehicle.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehicle.BadReactiveController;
import de.joachim.haensel.vehicle.ILowerLayerFactory;
import de.joachim.haensel.vehicle.IUpperLayerFactory;
import de.joachim.haensel.vehicle.NavigationController;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import sumobindings.JunctionType;
import sumobindings.LaneType;
import sumobindings.NetType;

public class VehicleCreationTest implements TestConstants
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
    public static void tearDownVrep() 
    {
        _vrep.simxFinish(_clientID);
    }
    
    @After
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }
    
    @Test
    public void testCreateAndDestroyVehicle() throws VRepException
    {
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        
        IUpperLayerFactory upperFact = () -> {return new NavigationController();};
        ILowerLayerFactory lowerFact = () -> {return new BadReactiveController();};

        float height = vehicleCreator.getVehicleHeight();
        vehicleCreator.createAt(-3f, 0, height + 0.1f, null, upperFact, lowerFact);
    }
    
    @Test
    public void testCreatePositionAndOrientateVehicle() throws VRepException
    {
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        float height = vehicleCreator.getVehicleHeight();
        
        IUpperLayerFactory upperFact = () -> {return new NavigationController();};
        ILowerLayerFactory lowerFact = () -> {return new BadReactiveController();};

        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f, null, upperFact, lowerFact);
        vehicle.setOrientation(1.0f, 1.0f, 1.0f);
        vehicle.setPosition(3.0f, 2.0f, 1.0f);
    }
    
    @Test
    public void testCreateAndPutOnNodeVehicle() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        VRepMap mapCreator = new VRepMap(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMap(roadMap);
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        float height = vehicleCreator.getVehicleHeight();
        
        IUpperLayerFactory upperFact = () -> {return new NavigationController();};
        ILowerLayerFactory lowerFact = () -> {return new BadReactiveController();};

        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f, roadMap, upperFact, lowerFact);
        NetType network = roadMap.getNetwork();
        JunctionType junction = network.getJunction().get(3);
        String incommingLaneID = junction.getIncLanes().split(" ")[0];
        LaneType incommingLane = roadMap.getLaneForName(incommingLaneID);
        vehicle.putOnJunctionHeadingTo(junction, incommingLane);
    }
}
