package de.joachim.haensel.phd.scenario.vehicle.test;



import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.Vehicle;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitVariableLookaheadController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepPartwiseVehicleCreator;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepPartwiseVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vrep.modelvisuals.MercedesVisualsNames;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import sumobindings.JunctionType;
import sumobindings.LaneType;
import sumobindings.NetType;

public class VehicleCreationTest implements TestConstants
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    private static List<IVehicle> _vehicles;
    
    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
        _vehicles = new ArrayList<IVehicle>();
    }

    @AfterAll
    public static void tearDownVrep() 
    {
        _vrep.simxFinish(_clientID);
    }
    
    @AfterEach
    public void cleanUpObjects() throws VRepException
    {
        _vehicles.forEach(vehicle -> vehicle.stop());
        _objectCreator.deleteAll();
    }
    
    @Test
    public void testCreateAndDestroyVehiclePartwise() throws VRepException
    {
        IVehicleFactory factory = new VRepPartwiseVehicleFactory(_vrep, _clientID, _objectCreator, 1.0f);
        IVehicleConfiguration vehicleConf = createConfiguration();
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        _vehicles.add(vehicle);
        System.out.println("wait here");
    }

    @Test
    public void testCreateAndDestroyVehicleLoadModel() throws VRepException
    {
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm");
        IVehicleConfiguration vehicleConf = createConfiguration();
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        _vehicles.add(vehicle);
        System.out.println("wait here");
    }

    @Test
    public void testCreateAndDestroyVehicleLoadModelWithAutoBody() throws VRepException
    {
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/carvisuals.ttm");
        IVehicleConfiguration vehicleConf = createConfiguration();
        List<String> autoBodyNames = new ArrayList<>();
        autoBodyNames.add(MercedesVisualsNames.AUTO_BODY_NAME);
        autoBodyNames.add(MercedesVisualsNames.REAR_LEFT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.REAR_RIGHT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.FRONT_LEFT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.FRONT_RIGHT_VISUAL);
        
        vehicleConf.setAutoBodyNames(autoBodyNames );
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        _vehicles.add(vehicle);
        System.out.println("wait here");
    }
    
    private IVehicleConfiguration createConfiguration()
    {
        IVehicleConfiguration vehicleConf = new VRepVehicleConfiguration();
        IUpperLayerFactory upperFact = () -> {return new DefaultNavigationController(2.0, 30.0);};
        ILowerLayerFactory lowerFact = () -> new PurePursuitVariableLookaheadController();
        vehicleConf.setUpperCtrlFactory(upperFact);
        vehicleConf.setLowerCtrlFactory(lowerFact);
        vehicleConf.setPosition(0.0, 0.0, 3.0);
        return vehicleConf;
    }
    
    @Test
    public void testCreatePositionAndOrientateVehicle() throws VRepException
    {
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, 1.0f);
        float height = vehicleCreator.getVehicleHeight();
        
        IUpperLayerFactory upperFact = () -> {return new DefaultNavigationController(2.0, 30.0);};
        ILowerLayerFactory lowerFact = () -> new PurePursuitVariableLookaheadController();

        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f, null, upperFact, lowerFact);
        vehicle.setOrientation(1.0f, 1.0f, 1.0f);
        vehicle.setPosition(3.0f, 2.0f, 1.0f);
    }
    
    @Test
    public void testScaledCreateVehicle() throws VRepException
    {
        float scaleFactor = 0.1f;
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, scaleFactor);
        float height = vehicleCreator.getVehicleHeight();
        
        
        IUpperLayerFactory upperFact = () -> new DefaultNavigationController(2.0 * scaleFactor, 30.0);
        ILowerLayerFactory lowerFact = () -> new PurePursuitVariableLookaheadController();

        vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + scaleFactor, null, upperFact, lowerFact);
        System.out.println("look at me");
    }

    @Test
    public void testCreateAndPutOnNodeVehicle() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0, 0.0);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        
        mapCreator.createSimplesShapeBasedMap(roadMap);
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, 1.0f);
        float height = vehicleCreator.getVehicleHeight();
        
        IUpperLayerFactory upperFact = () -> new DefaultNavigationController(2.0, 30.0);
        ILowerLayerFactory lowerFact = () -> new PurePursuitVariableLookaheadController();

        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f, roadMap, upperFact, lowerFact);
        NetType network = roadMap.getNetwork();
        JunctionType junction = network.getJunction().get(3);
        String incommingLaneID = junction.getIncLanes().split(" ")[0];
        LaneType incommingLane = roadMap.getLaneForName(incommingLaneID);
        vehicle.putOnJunctionHeadingTo(junction, incommingLane);
    }
}
