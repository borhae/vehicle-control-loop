package de.joachim.haensel.sumo2vrep.test;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.sumo2vrep.MapCreator;


class MapCreationTest
{
//  private static final String NETWORK_FILE_NAME = "./res/exampleMap/1stTestMap.net.xml";
//  private static final String NETWORK_FILE_NAME = "./res/exampleMap/testMap5Streets.net.xml";

    private static final float DOWN_SCALE_FACTOR = 1;
    private static final float STREET_WIDTH = 3.3f / DOWN_SCALE_FACTOR;
    private static final float STREET_HEIGHT = 0.4f / DOWN_SCALE_FACTOR;
    private VRepRemoteAPI _vrep;
    private int _clientID;

    @BeforeClass
    void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
    }
    
    @Before
    void setUp() throws Exception
    {
    }

    @After
    void tearDown() throws Exception
    {
        
    }

    @Test
    void testLoadAndRemoveMap() throws VRepException
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID);
        mapCreator.loadFunctions();
        mapCreator.createMap("./res/exampleMap/superSimpleMap.net.xml");
        mapCreator.deleteAll();
    }
    
    @Test
    void testLoadSimpleMap()
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID);
        mapCreator.loadFunctions();
        mapCreator.createMap("./res/exampleMap/superSimpleMap.net.xml");
    }

    @Test
    void testLoadRealWorldMap()
    {
        MapCreator mapCreator = new MapCreator(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID);
        mapCreator.loadFunctions();
        mapCreator.createMap("./res/exampleMap/neumarkRealWorldJustCars.net.xml");
    }
}
