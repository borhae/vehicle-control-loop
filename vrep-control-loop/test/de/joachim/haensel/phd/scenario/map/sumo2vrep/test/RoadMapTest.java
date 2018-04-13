package de.joachim.haensel.phd.scenario.map.sumo2vrep.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.FloatWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import sumobindings.EdgeType;
import sumobindings.LaneType;

public class RoadMapTest implements TestConstants
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
    public void testLineDistance() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0, 0.0);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        
        ShapeParameters dummyObjParams = new ShapeParameters();
        dummyObjParams.setName("dummy");
        dummyObjParams.setPosition(1.0f, 2.0f, 3.0f);
        
        int dummyID = _objectCreator.createPrimitive(dummyObjParams);
        
        FloatWA pos3d = new FloatWA(3);
        _vrep.simxGetObjectPosition(_clientID, dummyID, -1, pos3d, remoteApi.simx_opmode_blocking);
        Position2D pos2d = new Position2D(pos3d);
        
        LaneType closestLaneFor = roadMap.getClosestLaneFor(pos2d);
    }
    
    @Test
    public void testMapScaling()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        List<EdgeType> edges = roadMap.getEdges();
        System.out.println("before transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));

        roadMap.transform(new TMatrix(1/93.5, 0.0, 0.0));
        edges = roadMap.getEdges();
        System.out.println("after transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));
    }
    
    @Test
    public void testMapScalingVisualTest() throws VRepException
    {
        double scaleFactor = 0.5;

        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        List<EdgeType> edges = roadMap.getEdges();
        System.out.println("before transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);

        roadMap.transform(new TMatrix(scaleFactor, 0.0, 0.0));
        edges = roadMap.getEdges();
        System.out.println("after transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));
        mapCreator.setStreetWidthAndHeight(STREET_WIDTH * (float)scaleFactor, STREET_HEIGHT * (float)scaleFactor);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        System.out.println("do we have two different sized maps?");
    }
    
    
    @Test
    public void testMapOffsetVisualTest() throws VRepException
    {
        double scaleFactor = 1.0;

        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        List<EdgeType> edges = roadMap.getEdges();
        System.out.println("before transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);

        roadMap.transform(new TMatrix(scaleFactor, -50.0, -50.0));
        edges = roadMap.getEdges();
        System.out.println("after transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));
        mapCreator.setStreetWidthAndHeight(STREET_WIDTH * (float)scaleFactor, STREET_HEIGHT * (float)scaleFactor);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        System.out.println("do we have two different sized maps?");
    }

    @Test
    public void testMapCenterVisualTest() throws VRepException
    {
        double scaleFactor = 1.0;

        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        List<EdgeType> edges = roadMap.getEdges();
        System.out.println("before transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);

        roadMap.center(0.0, 0.0);
        edges = roadMap.getEdges();
        System.out.println("after transform");
        edges.stream().filter(edge -> isInternal(edge)).forEach(edge -> System.out.println(edge.getLane().get(0).getLength()));
        mapCreator.setStreetWidthAndHeight(STREET_WIDTH * (float)scaleFactor, STREET_HEIGHT * (float)scaleFactor);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        System.out.println("do we have two different sized maps?");
    }

    private boolean isInternal(EdgeType edge)
    {
        String function = edge.getFunction();
        return function == null ? true : !function.equals("internal");
    }
}
