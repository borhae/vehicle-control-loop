package de.joachim.haensel.vrephshapecreation.test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

public class TestVRepObjectCreation
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
    public void testSingleHandleResolve()
    {
        try
        {
            ShapeParameters params = new ShapeParameters();
            params.setIsDynamic(false);
            params.setIsRespondable(false);
            String objectName = "TestNameToIdentify";
            params.setName(objectName);
            params.setSize(1.0f, 1.0f, 1.0f);
            params.setType(EVRepShapes.CUBOID);
            params.setVisibility(true);
            _objectCreator.createPrimitive(params);
            int handleForName = _objectCreator.getHandleForName(objectName);
            System.out.println("Handle value" + handleForName);
            assertThat(handleForName, is(not(-1)));
        }
        catch (VRepException exc)
        {
           fail(exc);
        }
    }
    
    @Test
    public void testMultipleHandleResolve()
    {
        try
        {
            ShapeParameters params = new ShapeParameters();
            params.setIsDynamic(false);
            params.setIsRespondable(false);
            String objectName1 = "name1";
            String objectName2 = "name2";
            params.setName(objectName1);
            params.setSize(1.0f, 1.0f, 1.0f);
            params.setType(EVRepShapes.CUBOID);
            params.setVisibility(true);
            _objectCreator.createPrimitive(params);
            params.setName(objectName2);
            _objectCreator.createPrimitive(params);
            
            List<String> names = Arrays.asList(new String[]{objectName1, objectName2});
            List<Integer> handles= _objectCreator.getHandlesForNames(names);
            assertThat(handles, is(not(empty())));
        }
        catch (VRepException exc)
        {
           fail(exc);
        }
    }
}
