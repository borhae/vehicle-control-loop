package de.joachim.haensel.phd.scenario.lua.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.dummy.DummyParameters;

public class TestRemoteLuaObjectCreationFunctions
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    private static final String VREP_LOADING_SCRIPT_PARENT_OBJECT = "ScriptLoader";

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
    public void testCreateDummy() throws VRepException
    {
        DummyParameters params = new DummyParameters();
        params.setName("TestDummy");
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setPosition(0.0f, 0.0f, 0.0f);
        params.setSize(2.0f);
        _objectCreator.createDummy(params);
        System.out.println("Debug wait here if you want to see me");
    }
}
