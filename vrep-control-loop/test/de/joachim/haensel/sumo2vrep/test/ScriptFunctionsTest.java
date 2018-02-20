package de.joachim.haensel.sumo2vrep.test;

import java.math.BigInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class ScriptFunctionsTest
{
    private static final String PHYSICAL_CAR_BODY_NAME = "physicalCarBody";

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
    public void testDriveForward() throws VRepException
    {
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        float height = vehicleCreator.getVehicleHeight();
        Vehicle vehicle = vehicleCreator.createAt(-2.0f, 0.0f, height + 0.1f);

        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        
        vehicle.start();

        FloatWA inFloats = new FloatWA(2);
        inFloats.getArray()[0] = (float)0;
        inFloats.getArray()[1] = (float)50.0;
        _vrep.simxCallScriptFunction(_clientID, PHYSICAL_CAR_BODY_NAME, remoteApi.sim_scripttype_childscript, "control", null, inFloats, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

        vehicle.stop();
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
