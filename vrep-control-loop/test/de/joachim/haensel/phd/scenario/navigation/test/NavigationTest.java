package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.Color;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

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
    public void testNavigationOn3JunctionMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0, 0.0);
        
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);

        Position2D startPosition = new Position2D(11.4f, 101.4f);
        Position2D destinationPosition = new Position2D(101.81f, 9.23f);
        drawPosition(startPosition, Color.BLACK, _objectCreator, "startPosition");
        drawPosition(destinationPosition, Color.GREEN, _objectCreator, "destinationPosition");

        Navigator navigator = new Navigator(roadMap);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        drawRoute(route, _objectCreator);
    }

    @Test
    public void testNavigationRealMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        drawPosition(startPosition, Color.ORANGE, _objectCreator, "start");
        drawPosition(destinationPosition, Color.BLUE, _objectCreator, "goal");
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }
    
    @Test
    public void testNavigationRealMapClosestPointSelection() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        drawPosition(startPosition, Color.ORANGE, _objectCreator, "start");
        drawPosition(destinationPosition, Color.BLUE, _objectCreator, "goal");

        Position2D closestToStartOnMap = roadMap.getClosestPointOnMap(startPosition);
        Position2D closestToDestinationOnMap = roadMap.getClosestPointOnMap(destinationPosition);
        
        drawPosition(closestToStartOnMap, Color.ORANGE, _objectCreator, "startOnMap");
        drawPosition(closestToDestinationOnMap, Color.BLUE, _objectCreator, "goalOnMap");
        
        Navigator navigator = new Navigator(roadMap);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }
    
    @Test
    public void test3JunctionMapClosestPointSelection() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        VRepMap mapCreator = new VRepMap(1.3f, 0.5f, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(39.18,71.10);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        Position2D closestToStartOnMap = roadMap.getClosestPointOnMap(startPosition);
        Position2D closestToDestinationOnMap = roadMap.getClosestPointOnMap(destinationPosition);
        
        drawPosition(startPosition, Color.ORANGE, _objectCreator, "start");
        drawPosition(destinationPosition, Color.BLUE, _objectCreator, "goal");
        drawPosition(closestToStartOnMap, Color.ORANGE, _objectCreator, "startOnMap");
        drawPosition(closestToDestinationOnMap, Color.BLUE, _objectCreator, "goalOnMap");
        
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }
    
    @Test
    public void test3JunctionMapClosestPointSelectionPointFar() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        VRepMap mapCreator = new VRepMap(1.3f, 0.5f, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(29.38,91.10);
        Position2D destinationPosition = new Position2D(40.0, -80);
        drawPosition(startPosition, Color.ORANGE, _objectCreator, "start");
        drawPosition(destinationPosition, Color.BLUE, _objectCreator, "goal");
        Position2D closestToStartOnMap = roadMap.getClosestPointOnMap(startPosition);
        Position2D closestToDestinationOnMap = roadMap.getClosestPointOnMap(destinationPosition);
        
        drawPosition(closestToStartOnMap, Color.ORANGE, _objectCreator, "startOnMap");
        drawPosition(closestToDestinationOnMap, Color.BLUE, _objectCreator, "goalOnMap");
        
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }

    @Test
    public void testTargetAfterJunctionUTurn() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = 
                SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Position2D startPosition = new Position2D(3971.66, 4968.91).transform(centerMatrix);
        Position2D destinationPosition = new Position2D(2998.93, 4829.77).transform(centerMatrix);

        drawPosition(startPosition, Color.ORANGE, _objectCreator, "start");
        drawPosition(destinationPosition, Color.BLUE, _objectCreator, "goal");

        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        Position2D closestToStartOnMap = roadMap.getClosestPointOnMap(startPosition);
        Position2D closestToDestinationOnMap = roadMap.getClosestPointOnMap(destinationPosition);
        
        drawPosition(closestToStartOnMap, Color.ORANGE, _objectCreator, "startOnMap");
        drawPosition(closestToDestinationOnMap, Color.BLUE, _objectCreator, "goalOnMap");
        
        Navigator navigator = new Navigator(roadMap);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }
    
    @Test
    public void testSourceBeforeJunctionUTurn() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = 
                SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
      
        Position2D startPosition = new Position2D(2998.93, 4829.77).transform(centerMatrix);
        Position2D destinationPosition = new Position2D(3246.30, 2117.18).transform(centerMatrix);

        drawPosition(startPosition, Color.ORANGE, _objectCreator, "start");
        drawPosition(destinationPosition, Color.BLUE, _objectCreator, "goal");

        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        Position2D closestToStartOnMap = roadMap.getClosestPointOnMap(startPosition);
        Position2D closestToDestinationOnMap = roadMap.getClosestPointOnMap(destinationPosition);
        
        drawPosition(closestToStartOnMap, Color.ORANGE, _objectCreator, "startOnMap");
        drawPosition(closestToDestinationOnMap, Color.BLUE, _objectCreator, "goalOnMap");
        
        Navigator navigator = new Navigator(roadMap);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        drawRoute(route, _objectCreator);
        System.out.println("done");
    }
    
    @Test
    public void test180DegreeTurnDiagonal() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Navigator navigator = new Navigator(mapAndCenterMatrix.getRoadMap());

        Position2D start1 = new Position2D(8262.06,3246.62).transform(centerMatrix);
        Position2D destination1 = new Position2D(8283.08,3213.24).transform(centerMatrix);
        List<Line2D> route1 = navigator.getRoute(start1, destination1);
        drawRoute(route1, _objectCreator, "route1_");
        
        Position2D start2 = new Position2D(8215.48,3303.46).transform(centerMatrix);
        Position2D destination2 = new Position2D(8218.85,3305.09).transform(centerMatrix);
        List<Line2D> route2 = navigator.getRoute(start2, destination2);
        drawRoute(route2, _objectCreator, "route2_");

        Position2D start3 = new Position2D(8215.94,3273.68).transform(centerMatrix);
        Position2D destination3 = new Position2D(8193.96,3262.87).transform(centerMatrix);
        List<Line2D> route3 = navigator.getRoute(start3, destination3);
        drawRoute(route3, _objectCreator, "route3_");
        System.out.println("wait here");
    }
    
    @Test
    public void test180DegreeTurn90Rectangular() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Navigator navigator = new Navigator(mapAndCenterMatrix.getRoadMap());

        Position2D start1 = new Position2D(2488.35,2913.97).transform(centerMatrix);
        Position2D destination1 = new Position2D(2496.21,2909.68).transform(centerMatrix);
        List<Line2D> route1 = navigator.getRoute(start1, destination1);
        drawRoute(route1, _objectCreator, "route1_");
        
        Position2D start2 = new Position2D(2453.71,2910.47).transform(centerMatrix);
        Position2D destination2 = new Position2D(2449.42,2913.65).transform(centerMatrix);
        List<Line2D> route2 = navigator.getRoute(start2, destination2);
        drawRoute(route2, _objectCreator, "route2_");

        Position2D start3 = new Position2D(2474.13,2900.47).transform(centerMatrix);
        Position2D destination3 = new Position2D(2470.87,2899.67).transform(centerMatrix);
        List<Line2D> route3 = navigator.getRoute(start3, destination3);
        drawRoute(route3, _objectCreator, "route3_");

        Position2D start4 = new Position2D(2406.44,2927.00).transform(centerMatrix);
        Position2D destination4 = new Position2D(2410.41,2928.67).transform(centerMatrix);
        List<Line2D> route4 = navigator.getRoute(start4, destination4);
        drawRoute(route4, _objectCreator, "route4_");
        System.out.println("wait here");
    }
    
    private void drawPosition(Position2D position, Color orange, VRepObjectCreation objectCreator, String name) throws VRepException
    {
        ShapeParameters shapeParams = new ShapeParameters();
        shapeParams.setIsDynamic(false);
        shapeParams.setIsRespondable(false);
        shapeParams.setMass((float)1.0);
        shapeParams.setName(name);
        shapeParams.setOrientation((float)0.0, (float)0.0, (float)0.0);
        shapeParams.setPosition((float)position.getX(), (float)position.getY(), (float)0.0);
        shapeParams.setRespondableMask(ShapeParameters.GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        shapeParams.setSize(3.0f, 3.0f, 3.0f);
        shapeParams.setType(EVRepShapes.SPHERE);
        shapeParams.setVisibility(true);
        objectCreator.createPrimitive(shapeParams);
    }

    private void drawRoute(List<Line2D> route, VRepObjectCreation objectCreator, String routeName)
    {
        Color color = new Color(255, 0, 0);
        route.stream().map(IndexAdder.indexed()).forEachOrdered(indexedLine -> {
            try
            {
                objectCreator.createLine(indexedLine.v(), DOWN_SCALE_FACTOR, 1.0f, 0.5f, routeName + indexedLine.idx(), color);
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        });
    }

    private void drawRoute(List<Line2D> route, VRepObjectCreation objectCreator)
    {
        Color color = new Color(255, 0, 0);
        route.stream().map(IndexAdder.indexed()).forEachOrdered(indexedLine -> {
            try
            {
                objectCreator.createLine(indexedLine.v(), DOWN_SCALE_FACTOR, 1.0f, 0.5f, "someline_" + indexedLine.idx(), color);
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        });
    }
}
