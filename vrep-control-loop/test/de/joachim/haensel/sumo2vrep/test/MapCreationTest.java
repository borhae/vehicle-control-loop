package de.joachim.haensel.sumo2vrep.test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.sumo2vrep.MapCreator;


public class MapCreationTest
{
//  private static final String NETWORK_FILE_NAME = "./res/exampleMap/1stTestMap.net.xml";
//  private static final String NETWORK_FILE_NAME = "./res/exampleMap/testMap5Streets.net.xml";

    private static final float DOWN_SCALE_FACTOR = 1;
    private static final float STREET_WIDTH = 3.3f / DOWN_SCALE_FACTOR;
    private static final float STREET_HEIGHT = 0.4f / DOWN_SCALE_FACTOR;
    private static VRepRemoteAPI _vrep;
    private static int _clientID;

    @BeforeClass
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
    }
    
    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
        
    }
    
    @Test 
    public void testSimpleVrepObjectCreation()
    {
        StringWA inParamsString = new StringWA(1);
        String scriptText = null;
        try
        {
            try
            {
                scriptText = new String(Files.readAllBytes(Paths.get("./lua/TestScript.lua")));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }
            inParamsString.getArray()[0] = scriptText;
            StringWA returnStrings = new StringWA(1);
            _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", 6, "loadCode", null, null, inParamsString, null, null, null, returnStrings, null, remoteApi.simx_opmode_blocking);
            if (returnStrings.getArray().length >= 1)
            {
                String loadReturnValue = returnStrings.getArray()[0];
                System.out.println("script handle: " + loadReturnValue);
            }
        }
        catch (VRepException e)
        {
            fail("should not happen");
        }
    }

    @Test
    public void testLoadAndRemoveMap() throws VRepException
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID);
        mapCreator.loadFunctions();
        mapCreator.createMap("./res/roadnetworks/superSimpleMap.net.xml");
        mapCreator.deleteAll();
    }
    
    @Test
    public void testLoadSimpleMap()
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID);
        mapCreator.loadFunctions();
        mapCreator.createMap("./res/roadnetworks/superSimpleMap.net.xml");
    }

    @Test
    public void testLoadRealWorldMap()
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID);
        mapCreator.loadFunctions();
        mapCreator.createMap("./res/exampleMap/neumarkRealWorldJustCars.net.xml");
    }
}
