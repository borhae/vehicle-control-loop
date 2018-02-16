package de.joachim.haensel.sumo2vrep.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.sumo2vrep.MapCreator;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class SubScenarioCreationTest
{
    private static final float DOWN_SCALE_FACTOR = 1;
    private static final float STREET_WIDTH = 3.3f / DOWN_SCALE_FACTOR;
    private static final float STREET_HEIGHT = 0.4f / DOWN_SCALE_FACTOR;

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
    public void testSetVehicleOnSimpleRoadNetwork() throws VRepException
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMap("./res/roadnetworks/superSimpleMap.net.xml");
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        float height = vehicleCreator.getVehicleHeight();
        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f);
        vehicle.setOrientation(0.0f, 0.0f, 0.0f);
        vehicle.setPosition(3.0f, 2.0f, 1.0f);
    }
    
    @Test
    public void testShortDrive() throws VRepException
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMap("./res/roadnetworks/superSimpleMap.net.xml");
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        float height = vehicleCreator.getVehicleHeight();
        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f);
        vehicle.setOrientation(0.0f, 0.0f, 0.0f);
        vehicle.setPosition(3.0f, 2.0f, 1.0f);
        vehicle.start();
        vehicle.driveToBlocking(4.0f, 2.0f);
    }
}
