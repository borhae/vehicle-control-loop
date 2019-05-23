package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.tasks.ITask;
import de.joachim.haensel.phd.scenario.tasks.creation.PointListTaskCreatorConfig;
import de.joachim.haensel.phd.scenario.tasks.creation.TaskCreator;
import de.joachim.haensel.phd.scenario.tasks.execution.TaskExecutor;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitControllerVariableLookahead;
import de.joachim.haensel.phd.scenario.vehicle.navigation.IRouteAdaptor;
import de.joachim.haensel.phd.scenario.vehicle.navigation.IRouteProperyDetector;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class NavigationTest implements TestConstants
{
    public enum Counter
    {
        INSTANCE;
        
        private Counter()
        {
        }
        
        private int _curCnt = 0;
        
        public String getNext()
        {
            _curCnt++;
            return "" + _curCnt;
        }

        public String getSame()
        {
            return "" + _curCnt;
        }
    }

    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    public static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";

    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }
    
    @AfterAll
    public static void tearDownVrep() 
    {
        _objectCreator.removeScriptloader();
        _vrep.simxFinish(_clientID);
    }
    
    @AfterEach
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
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
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
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
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
    
    @Test
    public void test180DegreeTurnToTheRight() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/luebeck-roads-lefthand.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Navigator navigator = new Navigator(mapAndCenterMatrix.getRoadMap());

        Position2D start1 = new Position2D(7094.17,8024.64).transform(centerMatrix);
        Position2D destination1 = new Position2D(7107.43,8043.50).transform(centerMatrix);
        List<Line2D> route1 = navigator.getRoute(start1, destination1);
        drawRoute(route1, _objectCreator, "route1_");
        
        Position2D start2 = new Position2D(7110.54,8046.09).transform(centerMatrix);
        Position2D destination2 = new Position2D(7090.23,8024.75).transform(centerMatrix);
        List<Line2D> route2 = navigator.getRoute(start2, destination2);
        drawRoute(route2, _objectCreator, "route2_");
        System.out.println("wait here");
    }
    
    @Test
    public void testChandigarhProblems() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Navigator navigator = new Navigator(mapAndCenterMatrix.getRoadMap());
        //was ok
        Position2D start1 = new Position2D(7064.25, 5628.49).transform(centerMatrix);
        Position2D destination1 = new Position2D(220.73, 3817.32).transform(centerMatrix);
        List<Line2D> route1 = navigator.getRoute(start1, destination1);
        drawRoute(route1, _objectCreator, "route1_");
        //was ok
        Position2D start2 = new Position2D(220.73, 3817.32).transform(centerMatrix);
        Position2D destination2 = new Position2D(7508.53, 6937.68).transform(centerMatrix);
        List<Line2D> route2 = navigator.getRoute(start2, destination2);
        drawRoute(route2, _objectCreator, "route2_");
        //needs fix
        Position2D start3 = new Position2D(6170.84, 4890.12).transform(centerMatrix);
        Position2D destination3 = new Position2D(13642.32, 5685.41).transform(centerMatrix);
        List<Line2D> route3 = navigator.getRoute(start3, destination3);
        drawRoute(route3, _objectCreator, "route3_");

        System.out.println("wait here");
    }

    @Test
    public void testChandigarhProblemsSegments() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        
        Navigator navigator = new Navigator(mapAndCenterMatrix.getRoadMap());
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, 120.0);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);

        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();

        //was ok
        Position2D start1 = new Position2D(7064.25, 5628.49).transform(centerMatrix);
        Position2D destination1 = new Position2D(220.73, 3817.32).transform(centerMatrix);
        List<Line2D> route1 = navigator.getRoute(start1, destination1);
        List<TrajectoryElement> trajectoryElements1 = trajectorizer.createTrajectory(route1);
        navigationListener.notifySegmentsChanged(trajectoryElements1);

        //was ok
        Position2D start2 = new Position2D(220.73, 3817.32).transform(centerMatrix);
        Position2D destination2 = new Position2D(7508.53, 6937.68).transform(centerMatrix);
        List<Line2D> route2 = navigator.getRoute(start2, destination2);
        List<TrajectoryElement> trajectoryElements2 = trajectorizer.createTrajectory(route2);
        navigationListener.notifySegmentsChanged(trajectoryElements2);
        //was ok
        Position2D start3 = new Position2D(6170.84, 4890.12).transform(centerMatrix);
        Position2D destination3 = new Position2D(13642.32, 5685.41).transform(centerMatrix);
        List<Line2D> route3 = navigator.getRoute(start3, destination3);
        List<TrajectoryElement> trajectoryElements3 = trajectorizer.createTrajectory(route3);
        navigationListener.notifySegmentsChanged(trajectoryElements3);

        System.out.println("wait here");
    }
    
    @Test
    public void testChandigarhProblemRoute1() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        
        Navigator navigator = new Navigator(roadMap);
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, 120.0);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);

        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
        
        Position2D startPos = new Position2D(12745.65, 4819.28).transform(centerMatrix);
        Position2D endPos = new Position2D(7451.17, 8104.12).transform(centerMatrix);
        
        
        EdgeType startEdge = roadMap.getClosestEdgeFor(startPos);
        EdgeType targetEdge = roadMap.getClosestEdgeFor(endPos);
        JunctionType startJunction = roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = roadMap.getJunctionForName(targetEdge.getFrom());
        List<Node> nodePath = navigator.computePath(startJunction, targetJunction);
        navigator.setSourceTarget(startPos, endPos);
        
        IRouteProperyDetector sharpTurnDetector = (result, curLine, nextLine, curLineV, nextLineV) -> navigator.isSharpTurn(curLineV, nextLineV);

        IRouteAdaptor sharpTurnRemover = (result, curLine, nextLine, curLineV, nextLineV) -> navigator.addTurnAroundCircle(result, curLine, nextLine);
        List<Line2D> linesRemovedSharpTurns  = navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnRemover);
        
        List<Position2D> sharpTurnIntersections = new ArrayList<Position2D>();
        IRouteAdaptor sharpTurnVisualizer = (result, curLine, nextLine, curLineV, nextLineV) -> sharpTurnIntersections.add(Position2D.between(curLine.getP2(), nextLine.getP1()));
        navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnVisualizer);
        
        sharpTurnIntersections.stream().map(IndexAdder.indexed()).forEachOrdered(idxPos -> drawPosition(idxPos.v(), Color.RED, _objectCreator, "route_" + idxPos.idx()));
        
        List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(linesRemovedSharpTurns);
        navigationListener.notifySegmentsChanged(trajectoryElements);

        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }
    
    @Test
    public void testChandigarhProblemRoute44() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.removed.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        Navigator navigator = new Navigator(roadMap);

        List<String> pointsAsString;
        try
        {
            pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Chandigarhpoints_spread.txt").toPath());
            List<Position2D> positions = pointsAsString.stream().map(string -> new Position2D(string).transform(centerMatrix)).collect(Collectors.toList());
    
            int lower = 43;
            int upper = 46;
            for(int idx = Math.max(0,  lower); (idx < Math.min(upper, positions.size() - 1)) && (lower < upper) ; idx++)
            {
                Position2D pos1 = positions.get(idx);
                Position2D pos2 = positions.get(idx + 1);
                String segName = Integer.toString(idx);
                VRepNavigationListener navigationListener = new VRepNavigationListener(_objectCreator, () -> segName);
                drawRoute(navigator, pos1, pos2, navigationListener);
            }
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
   }
    
    @Test
    public void testChandigarhProblemRoute54PartOnly() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.removed.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        Navigator navigator = new Navigator(roadMap);

        List<String> pointsAsString;
        pointsAsString = Arrays.asList(new String[] {"8715.99,7152.40", "8440.86,7480.21"});
        List<Position2D> positions = pointsAsString.stream().map(string -> new Position2D(string).transform(centerMatrix)).collect(Collectors.toList());

        for(int idx = 0; (idx < positions.size() - 1); idx++)
        {
            Position2D pos1 = positions.get(idx);
            Position2D pos2 = positions.get(idx + 1);
            String segName = Integer.toString(idx);
            VRepNavigationListener navigationListener = new VRepNavigationListener(_objectCreator, () -> segName);
            drawRoute(navigator, pos1, pos2, navigationListener);

            System.out.println("Drawing done, now driving");
            TaskCreator taskCreator = new TaskCreator();
            PointListTaskCreatorConfig taskConfiguration = new PointListTaskCreatorConfig();
            double lookahead = 15.0;
            taskConfiguration.setControlParams(lookahead, 120.0, 3.8, 4.0, 0.8);
            taskConfiguration.setDebug(true);
            taskConfiguration.setMap(roadMap);
            taskConfiguration.configSimulator(_vrep, _clientID, _objectCreator);
            
            taskConfiguration.setCarModel("./res/simcarmodel/vehicleVisualsBrakeScript.ttm");
            
            taskConfiguration.setTargetPoints(Arrays.asList(new Position2D[] {pos1, pos2}));
            taskConfiguration.setLowerLayerController(() -> new PurePursuitControllerVariableLookahead());
            taskCreator.configure(taskConfiguration);
            List<ITask> tasks = taskCreator.createTasks();
            
            TaskExecutor executor = new TaskExecutor();
            executor.execute(tasks);
        }
        

        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }

    private void drawRoute(Navigator navigator, Position2D pos1, Position2D pos2, VRepNavigationListener navigationListener)
    {
        List<Line2D> route = navigator.getRoute(pos1, pos2);
        
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, 120.0);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);
        
        List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(route);
        navigationListener.activateSegmentDebugging();
        navigationListener.notifySegmentsChanged(trajectoryElements);
    }
    
    @Test
    public void showChandigarh183RoutesLoops() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        System.out.println("Center matrix: \n" + centerMatrix.toString());
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        
        Navigator navigator = new Navigator(mapAndCenterMatrix.getRoadMap());

        List<String> pointsAsString;
        try
        {
            pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Chandigarhpoints_spread.txt").toPath());
            List<Position2D> positions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());// runner.run("luebeck_183_max_scattered_targets", 15.0, 120.0, 3.8, 4.0, 0.8, positions, "luebeck-roads.net.xml", "blue");
            for(int idx = 0; idx < positions.size() - 1; idx++)
            {
                System.out.println("\nRoute: " + idx + ". ");
                Position2D startRaw = positions.get(idx);
                Position2D startPos = startRaw.transformCopy(centerMatrix);
                Position2D endRaw = positions.get(idx + 1);
                Position2D endPos = endRaw.transformCopy(centerMatrix);
                System.out.println("raw         start: " + startRaw.toString()  + ", end: " + endRaw.toString());
                System.out.println("transformed start: " + startPos.toString()  + ", end: " + endPos.toString());
                
                EdgeType startEdge = roadMap.getClosestEdgeFor(startPos);
                EdgeType targetEdge = roadMap.getClosestEdgeFor(endPos);
                JunctionType startJunction = roadMap.getJunctionForName(startEdge.getTo());
                JunctionType targetJunction = roadMap.getJunctionForName(targetEdge.getFrom());
                List<Node> nodePath = navigator.computePath(startJunction, targetJunction);
                navigator.setSourceTarget(startPos, endPos);
                
                IRouteProperyDetector sharpTurnDetector = (result, curLine, nextLine, curLineV, nextLineV) -> navigator.isSharpTurn(curLineV, nextLineV);

                IRouteAdaptor sharpTurnRemover = (result, curLine, nextLine, curLineV, nextLineV) -> navigator.addTurnAroundCircle(result, curLine, nextLine);
                List<Line2D> linesRemovedSharpTurns  = navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnRemover);
                
                List<Position2D> sharpTurnIntersections = new ArrayList<Position2D>();
                IRouteAdaptor sharpTurnVisualizer = (result, curLine, nextLine, curLineV, nextLineV) -> sharpTurnIntersections.add(Position2D.between(curLine.getP2(), nextLine.getP1()));
                navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnVisualizer);
                
                final int routeIdx = idx;
                sharpTurnIntersections.stream().map(IndexAdder.indexed()).forEachOrdered(idxPos -> drawPosition(idxPos.v(), Color.RED, _objectCreator, "route_" + routeIdx + "_" + idxPos.idx()));
                
                ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
                IVelocityAssignerFactory velocityFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, 120.0);
                ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);
                
                List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(linesRemovedSharpTurns);
                INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
                navigationListener.activateSegmentDebugging();
                navigationListener.notifySegmentsChanged(trajectoryElements);
            }
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }

    @Test
    public void identifyChandigarh183RoutesMulitloops() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        System.out.println("Center matrix: \n" + centerMatrix.toString());
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();
        
        Navigator navigator = new Navigator(mapAndCenterMatrix.getRoadMap());

        List<String> pointsAsString;
        try
        {
            pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Chandigarhpoints_spread.txt").toPath());
            List<Position2D> positions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());// runner.run("luebeck_183_max_scattered_targets", 15.0, 120.0, 3.8, 4.0, 0.8, positions, "luebeck-roads.net.xml", "blue");
            for(int idx = 0; idx < positions.size() - 1; idx++)
            {
                System.out.println("\nRoute: " + idx + ". ");
                Position2D startRaw = positions.get(idx);
                Position2D startPos = startRaw.transformCopy(centerMatrix);
                Position2D endRaw = positions.get(idx + 1);
                Position2D endPos = endRaw.transformCopy(centerMatrix);
                System.out.println("raw         start: " + startRaw.toString()  + ", end: " + endRaw.toString());
                System.out.println("transformed start: " + startPos.toString()  + ", end: " + endPos.toString());
                
                EdgeType startEdge = roadMap.getClosestEdgeFor(startPos);
                EdgeType targetEdge = roadMap.getClosestEdgeFor(endPos);
                JunctionType startJunction = roadMap.getJunctionForName(startEdge.getTo());
                JunctionType targetJunction = roadMap.getJunctionForName(targetEdge.getFrom());
                List<Node> nodePath = navigator.computePath(startJunction, targetJunction);
                navigator.setSourceTarget(startPos, endPos);
                
                IRouteProperyDetector sharpTurnDetector = (result, curLine, nextLine, curLineV, nextLineV) -> navigator.isSharpTurn(curLineV, nextLineV);

                IRouteAdaptor sharpTurnRemover = (result, curLine, nextLine, curLineV, nextLineV) -> navigator.addTurnAroundCircle(result, curLine, nextLine);
                List<Line2D> linesRemovedSharpTurns  = navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnRemover);
                
                List<Position2D> sharpTurnIntersections = new ArrayList<Position2D>();
                IRouteAdaptor sharpTurnCollector = (result, curLine, nextLine, curLineV, nextLineV) -> sharpTurnIntersections.add(Position2D.between(curLine.getP2(), nextLine.getP1()));
                navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnCollector);
                
                final int routeIdx = idx;
                List<int[]> multiloops = multiloops(sharpTurnIntersections);
                multiloops.forEach(loopIdxs -> 
                    {
                        drawPosition(sharpTurnIntersections.get(loopIdxs[0]), Color.RED, _objectCreator, "route_" + routeIdx + "_a");
                        drawPosition(sharpTurnIntersections.get(loopIdxs[1]), Color.RED, _objectCreator, "route_" + routeIdx + "_b");
                    });
                
                ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
                IVelocityAssignerFactory velocityFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, 120.0);
                ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);
                
                List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(linesRemovedSharpTurns);
                INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
                navigationListener.activateSegmentDebugging();
                navigationListener.notifySegmentsChanged(trajectoryElements);
            }
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }

    @Test
    public void identifyChandigarh183RoutesMulitloopsParallel() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        System.out.println("Center matrix: \n" + centerMatrix.toString());
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();

        try
        {
            List<String> positionsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Chandigarhpoints_spread.txt").toPath());
            List<Position2D> positions = positionsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());// runner.run("luebeck_183_max_scattered_targets", 15.0, 120.0, 3.8, 4.0, 0.8, positions, "luebeck-roads.net.xml", "blue");
            List<NumberedRoute> routes = new ArrayList<NumberedRoute>();
            for(int idx = 0; idx < positions.size() - 1; idx++)
            {
                Position2D start = positions.get(idx);
                Position2D end = positions.get(idx + 1);
                NumberedRoute newRoute = new NumberedRoute(start, end, idx);
                routes.add(newRoute);
            }
            List<MultiLoopDetectionResult> results = routes.parallelStream().map(route -> computeRouteIdentifyMultiLoop(centerMatrix, roadMap, route)).collect(Collectors.toList());
            
            results.forEach(result -> drawMultiloopCenterAndRouteInSimulator(result));
            results.forEach(result -> printOnStdOut(result));
            
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }

    @Test
    public void identifyEdgesInChandigarh183RoutesMulitloopsParallel() throws VRepException
    {
        RoadMapAndCenterMatrix mapAndCenterMatrix = SimulationSetupConvenienceMethods.createCenteredMap(_clientID, _vrep, _objectCreator, "./res/roadnetworks/chandigarh-roads-lefthand.removed.net.xml");
        TMatrix centerMatrix = mapAndCenterMatrix.getCenterMatrix();
        System.out.println("Center matrix: \n" + centerMatrix.toString());
        RoadMap roadMap = mapAndCenterMatrix.getRoadMap();

        try
        {
            List<String> positionsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Chandigarhpoints_spread.txt").toPath());
            List<Position2D> positions = positionsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());// runner.run("luebeck_183_max_scattered_targets", 15.0, 120.0, 3.8, 4.0, 0.8, positions, "luebeck-roads.net.xml", "blue");
            List<NumberedRoute> routes = new ArrayList<NumberedRoute>();
            for(int idx = 0; idx < positions.size() - 1; idx++)
            {
                Position2D start = positions.get(idx);
                Position2D end = positions.get(idx + 1);
                NumberedRoute newRoute = new NumberedRoute(start, end, idx);
                routes.add(newRoute);
            }
            List<MultiLoopDetectionResult> results = routes.parallelStream().map(route -> computeEdgePathIdentifyMultiLoop(centerMatrix, roadMap, route)).collect(Collectors.toList());
            System.out.format("printing results: %d", results.size());
            results.forEach(result -> printOnStdOut(result));
            results.forEach(result -> drawMultiloopCenterAndRouteInSimulator(result));
        } 
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("enter arbitrary stuff an then press enter");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        System.out.println(input);
        scanner.close();
    }

    private MultiLoopDetectionResult computeEdgePathIdentifyMultiLoop(TMatrix centerMatrix, RoadMap roadMap, NumberedRoute route)
    {
        Position2D startRaw = route.getStart();
        Position2D endRaw = route.getEnd();
        Navigator navigator = new Navigator(roadMap);
        Position2D startPos = startRaw.transformCopy(centerMatrix);
        Position2D endPos = endRaw.transformCopy(centerMatrix);
        
        EdgeType startEdge = roadMap.getClosestEdgeFor(startPos);
        EdgeType targetEdge = roadMap.getClosestEdgeFor(endPos);
        JunctionType startJunction = roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = roadMap.getJunctionForName(targetEdge.getFrom());
        navigator.setSourceTarget(startPos, endPos);

        // nodes
        List<Node> nodePath = navigator.computePath(startJunction, targetJunction);
        // edges and lines (keep both)
        List<EdgeLine> edgeLineResult = new ArrayList<>();
        List<EdgeType> edgePath = navigator.transformNodePathToEdgePath(nodePath, startEdge, targetEdge);
        for(int idx = 0; idx < edgePath.size(); idx++)
        {
            EdgeType curEdge = edgePath.get(idx);
            List<LaneType> lanes = curEdge.getLane();
            String shape = lanes.get(0).getShape();
            List<Line2D> linesToAdd = Line2D.createLines(shape);
            List<EdgeLine> edgeLines = linesToAdd.stream().map(line -> new EdgeLine(line, curEdge)).collect(Collectors.toList());
            edgeLineResult.addAll(edgeLines);
        }
        // sharp turn detection and memorization
        IRouteProperyDetector sharpTurnDetector = (resultLines, curLine, nextLine, curLineV, nextLineV) -> navigator.isSharpTurn(curLineV, nextLineV);
        IEdgeLineAdaptor sharpTurnMarker = (curIdx, nxtIdx, curEdgeLine, nextEdgeLine, curLine, nextLine) -> {                    
            curEdgeLine.markAsStartOfSharpTurn();
            curEdgeLine.setEndOfSharpTurnEdgeLine(nextEdgeLine);
            nextEdgeLine.markAsEndOfSharpTurn();
            if(curEdgeLine.getEdge() == nextEdgeLine.getEdge())
            {
                System.out.println("Turn on a single edge betwen lines of that turn");
                if(curLine == nextLine)
                {
                    System.out.println("weird because lines are the same");
                }
                else
                {
                    System.out.print("current: " + curLine.toString() + ", next: " + nextLine.toString());
                }
            }
            Position2D center = Position2D.between(curLine.getP2(), nextLine.getP1());
            curEdgeLine.setCenter(center);
        };
        traverse(edgeLineResult, sharpTurnMarker, sharpTurnDetector);

        // identify multiloops in loops
        List<EdgeLine> sharpTurnEdges = edgeLineResult.stream().filter(edgeLine -> (edgeLine.isSharpTurn() && edgeLine.getCenter() != null)).collect(Collectors.toList());
        int n = sharpTurnEdges.size();
        List<int[]> multiLoopIdxs = new ArrayList<int[]>();
        Map<Integer, EdgeLine> idxToEdgeMap = new HashMap<Integer, EdgeLine>();
        for(int idxI = 0; idxI < n; idxI++)
        {
            for(int idxJ = 0; idxJ < n; idxJ++)
            {
                if(idxI != idxJ)
                {
                    EdgeLine edgeLineI = sharpTurnEdges.get(idxI);
                    Position2D p1 = edgeLineI.getCenter();
                    EdgeLine edgeLineJ = sharpTurnEdges.get(idxJ);
                    Position2D p2 = edgeLineJ.getCenter();
                    double distance = Position2D.distance(p1, p2);
                    if(distance < 50.0)
                    {
                        final int i = idxI;
                        final int j = idxJ;
                        boolean alreadyInList = multiLoopIdxs.stream().map(cur -> (i == cur[1]) && (j == cur[0])).reduce((a, b) -> a || b).orElse(false);
                        if(!alreadyInList)
                        {
                            multiLoopIdxs.add(new int[] {idxI, idxJ});
                            edgeLineI.setMultiLoopPart();
                            edgeLineI.setOtherMultiLoopPart(edgeLineJ);
                            edgeLineJ.setMultiLoopPart();
                            idxToEdgeMap.put(i, edgeLineI);
                            idxToEdgeMap.put(j, edgeLineJ);
                        }
                    }
                }
            }
        }

        // compute turnarounds for visualization
        IRouteAdaptor sharpTurnRemover = (result, curLine, nextLine, curLineV, nextLineV) -> navigator.addTurnAroundCircle(result, curLine, nextLine);
        List<Line2D> linesRemovedSharpTurns = navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnRemover);

        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, 120.0);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);
        
        
        List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(linesRemovedSharpTurns);
        String id = Integer.toString(route.getRouteNumber());
        MultiLoopDetectionResult result = new MultiLoopDetectionResult(trajectoryElements, sharpTurnEdges, multiLoopIdxs, idxToEdgeMap, id);
        return result;
    }

    private List<EdgeLine> traverse(List<EdgeLine> rawEdgeLineResult, IEdgeLineAdaptor adaptor, IRouteProperyDetector routeProperty)
    {
        List<Line2D> rawResult = rawEdgeLineResult.stream().map(edgeLine -> edgeLine.getLine()).collect(Collectors.toList());
        List<EdgeLine> result = new ArrayList<>();
        for(int idx = 0; idx < rawEdgeLineResult.size(); idx++)
        {
            int curIdx = idx;
            int nxtIdx = curIdx + 1;
            EdgeLine curEdgeLine = rawEdgeLineResult.get(curIdx);
            Line2D curLine = curEdgeLine.getLine();
            result.add(curEdgeLine);
            if(nxtIdx < rawEdgeLineResult.size())
            {
                EdgeLine nextEdgeLine = rawEdgeLineResult.get(nxtIdx);
                Line2D nextLine = nextEdgeLine.getLine();
                
                Vector2D curLineV = new Vector2D(curLine);
                Vector2D nextLineV = new Vector2D(nextLine);
                if(routeProperty.holds(rawResult, curLine, nextLine, curLineV, nextLineV))
                {
                    adaptor.adapt(curIdx, nxtIdx, curEdgeLine, nextEdgeLine, curLine, nextLine);
                }
            }
        }
        return result;
    }

    private void printOnStdOut(MultiLoopDetectionResult result)
    {
        List<int[]> multiLoops = result.getMultiLoops();
        Map<Integer, EdgeLine> idxToEdgeMap = result.getIdxToEdgeMap();
        List<Position2D> intersections = result.getSharpTurnIntersections();
        if (!multiLoops.isEmpty())
        {
            String numLoopsStatement = multiLoops.size() == 1 ? "is  1 multi-loop " : String.format("are %d multi-loops", multiLoops.size());
            String positionStatement = "";
            for(int idx = 0; idx < multiLoops.size(); idx++)
            {
                int idxI = multiLoops.get(idx)[0];
                int idxJ = multiLoops.get(idx)[1];
                Position2D position = Position2D.between(intersections.get(idxI), intersections.get(idxJ));
                positionStatement += String.format("position (%.2f, %.2f)", position.getX(), position.getY());
                if(idxToEdgeMap != null)
                {
                    String edgeLineIID = idxToEdgeMap.get(idxI).getEdge().getId();
                    String edgeLineINxtID = idxToEdgeMap.get(idxI).getNextEdgeLine().getEdge().getId();
                    String edgeLineJID = idxToEdgeMap.get(idxJ).getEdge().getId();
                    String edgeLineJNxtID = idxToEdgeMap.get(idxJ).getNextEdgeLine().getEdge().getId();
                    positionStatement += String.format(" between edges: (%s, %s) and (%s, %s)", edgeLineIID, edgeLineINxtID, edgeLineJID, edgeLineJNxtID);
                }
                positionStatement += " ,";
            }
            System.out.println(String.format("There %s in route %s at %s", numLoopsStatement, result.getId(), positionStatement));
        }
    }

    private void drawMultiloopCenterAndRouteInSimulator(MultiLoopDetectionResult result)
    {
        List<int[]> multiLoops = result.getMultiLoops();
        
        multiLoops.stream().map(IndexAdder.indexed()).forEachOrdered(indexedLoops -> 
        {
            List<Position2D> sharpTurnIntersections = result.getSharpTurnIntersections();
            String resultID = result.getId();
            int idx = indexedLoops.idx();
            drawPosition(sharpTurnIntersections.get(indexedLoops.v()[0]), Color.RED, _objectCreator, "route_" + resultID + "_a" + "_" + idx);
            drawPosition(sharpTurnIntersections.get(indexedLoops.v()[1]), Color.RED, _objectCreator, "route_" + resultID + "_b" + "_" + idx);
        });
        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
        navigationListener.notifySegmentsChanged(result.getTrajectoryElements());
    }

    private MultiLoopDetectionResult computeRouteIdentifyMultiLoop(TMatrix centerMatrix, RoadMap roadMap, NumberedRoute route)
    {
        Position2D startRaw = route.getStart();
        Position2D endRaw = route.getEnd();
        Navigator navigator = new Navigator(roadMap);
        Position2D startPos = startRaw.transformCopy(centerMatrix);
        Position2D endPos = endRaw.transformCopy(centerMatrix);
        System.out.println("raw         start: " + startRaw.toString()  + ", end: " + endRaw.toString());
        System.out.println("transformed start: " + startPos.toString()  + ", end: " + endPos.toString());
        
        EdgeType startEdge = roadMap.getClosestEdgeFor(startPos);
        EdgeType targetEdge = roadMap.getClosestEdgeFor(endPos);
        JunctionType startJunction = roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = roadMap.getJunctionForName(targetEdge.getFrom());
        List<Node> nodePath = navigator.computePath(startJunction, targetJunction);
        navigator.setSourceTarget(startPos, endPos);
        
        IRouteProperyDetector sharpTurnDetector = (resultLines, curLine, nextLine, curLineV, nextLineV) -> navigator.isSharpTurn(curLineV, nextLineV);

        IRouteAdaptor sharpTurnRemover = (resultLines, curLine, nextLine, curLineV, nextLineV) -> navigator.addTurnAroundCircle(resultLines, curLine, nextLine);
        List<Line2D> linesRemovedSharpTurns  = navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnRemover);
        
        List<Position2D> sharpTurnIntersections = new ArrayList<Position2D>();
        IRouteAdaptor sharpTurnCollector = (resultLines, curLine, nextLine, curLineV, nextLineV) -> sharpTurnIntersections.add(Position2D.between(curLine.getP2(), nextLine.getP1()));
        navigator.createLinesFromPath(nodePath, startEdge, targetEdge, null, sharpTurnDetector, sharpTurnCollector);
        
        List<int[]> multiloops = multiloops(sharpTurnIntersections);
        
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, 120.0);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);
        
        List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(linesRemovedSharpTurns);
//        String id = String.format("P1_%.0f_%.0f_%.0f_%.0f", startRaw.getX(), startRaw.getY(), endRaw.getX(), endRaw.getY());
        String id = Integer.toString(route.getRouteNumber());
        MultiLoopDetectionResult result = new MultiLoopDetectionResult(trajectoryElements, sharpTurnIntersections, multiloops, id);
        return result;
    }

    private List<int[]> multiloops(List<Position2D> centers)
    {   
        List<int[]> result = new ArrayList<int[]>();
        int n = centers.size();
        for(int idxI = 0; idxI < n; idxI++)
        {
            for(int idxJ = 0; idxJ < n; idxJ++)
            {
                
                if(idxI != idxJ)
                {
                    Position2D p1 = centers.get(idxI);
                    Position2D p2 = centers.get(idxJ);
                    double distance = Position2D.distance(p1, p2);
                    if(distance < 50.0)
                    {
                        final int i = idxI;
                        final int j = idxJ;
                        boolean alreadyInList = result.stream().map(cur -> (i == cur[1]) && (j == cur[0])).reduce((a, b) -> a || b).orElse(false);
                        if(!alreadyInList)
                        {
                            result.add(new int[] {idxI, idxJ});
                        }
                    }
                }
            }
        }
        return result;
    }

    private void drawPosition(Position2D position, Color orange, VRepObjectCreation objectCreator, String name) 
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
        try
        {
            objectCreator.createPrimitive(shapeParams);
        }
        catch (VRepException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
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
