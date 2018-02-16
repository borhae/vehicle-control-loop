package de.joachim.haensel.sumo2vrep.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VehicleCreationTest
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
        float height = vehicleCreator.getVehicleHeight();
        vehicleCreator.createAt(-3f, 0, height + 0.1f);
    }
    
    @Test
    public void testCreatePositionAndOrientateVehicle() throws VRepException
    {
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        float height = vehicleCreator.getVehicleHeight();
        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + height + 0.1f);
        vehicle.setOrientation(1.0f, 1.0f, 1.0f);
        vehicle.setPosition(3.0f, 2.0f, 1.0f);
    }
}
