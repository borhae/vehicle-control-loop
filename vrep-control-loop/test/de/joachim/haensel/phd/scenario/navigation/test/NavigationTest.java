package de.joachim.haensel.phd.scenario.navigation.test;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.sumo2vrep.Segment;
import de.joachim.haensel.sumo2vrep.VRepMap;
import de.joachim.haensel.vehiclecontrol.Navigator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class NavigationTest
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
    public void testNavigationOn3JunctionMap()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        Navigator navigator = new Navigator(roadMap);
        List<Segment> route = navigator.getRoute(new Position2D(11.4f, 101.4f), new Position2D(101.81f, 9.23f));
        VRepMap mapCreator = new VRepMap(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMap(roadMap);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }

    private void drawRoute(List<Segment> route, VRepObjectCreation objectCreator)
    {
        route.stream().map(IndexAdder.indexed()).forEachOrdered(indexedSegment -> {
            try
            {
                objectCreator.createSegment(indexedSegment.value(), DOWN_SCALE_FACTOR, 1.0f, 0.1f, "segment_" + indexedSegment.index());
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        });
    }
}
