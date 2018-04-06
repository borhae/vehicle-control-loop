package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.Color;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.sumo2vrep.VRepMap;
import de.joachim.haensel.vehiclecontrol.Navigator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class NavigationTest implements TestConstants
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
    public void testNavigationOn3JunctionMap()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0, 0.0);
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(11.4f, 101.4f);
        Position2D destinationPosition = new Position2D(101.81f, 9.23f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        drawRoute(route, _objectCreator);
    }

    @Test
    public void testNavigationRealMap()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }
    
    private void drawRoute(List<Line2D> route, VRepObjectCreation objectCreator)
    {
        Color color = new Color(255, 0, 0);
        route.stream().map(IndexAdder.indexed()).forEachOrdered(indexedLine -> {
            try
            {
                objectCreator.createLine(indexedLine.v(), DOWN_SCALE_FACTOR, 1.0f, 0.1f, "someline_" + indexedLine.idx(), color);
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        });
    }
}
