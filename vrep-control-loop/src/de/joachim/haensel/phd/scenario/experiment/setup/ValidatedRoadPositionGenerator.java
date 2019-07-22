package de.joachim.haensel.phd.scenario.experiment.setup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DijkstraAlgo;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.NoVelocityAssigner;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class ValidatedRoadPositionGenerator
{
    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";
    private static VRepObjectCreation _objectCreator;
    private static int _numOfRoutes;

    public static void main(String[] args) throws VRepException
    {
        System.out.println("choose map: 1 and <enter> for Luebeck, 2 and <enter> for Chandigarh");
        Scanner scanner = new Scanner(System.in);
        String mapFileNameInput = scanner.next();
        String mapFileName = "";
        if(mapFileNameInput.equalsIgnoreCase("1"))
        {
            mapFileName = "luebeck-roads.net.xml";
        }
        else if(mapFileNameInput.equalsIgnoreCase("2"))
        {
            mapFileName = "chandigarh-roads-lefthand.removed.net.xml";
        }
        else
        {
            System.out.println("You need to decide :)!");
            scanner.close();
            return;
        }

        int numberOfPoints = 200;

        RoadMap roadMap = new RoadMap(RES_ROADNETWORKS_DIRECTORY + mapFileName);
        
        VRepRemoteAPI vrep = VRepRemoteAPI.INSTANCE;
        int clientID = vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(vrep, clientID);

        System.out.println("Choose generator seed (number and <enter>)");
        boolean correctSeedGiven = false;
        int seed = -1;
        String stringSeed = scanner.next();
        while(!correctSeedGiven)
        {
            try
            {
                seed = Integer.parseInt(stringSeed);
                correctSeedGiven = true;
            }
            catch (NumberFormatException exc) 
            {
                System.out.println("enter a positive integer number that is not too large (< 10000000000000)");
                correctSeedGiven = false;
                
                System.out.println("do you want to leave? y and <enter> to leave or a number and <enter> to give it another try");
                stringSeed = scanner.next();
                if(stringSeed.equalsIgnoreCase("y"))
                {
                    scanner.close();
                    return;
                }
            }
        }

        MersenneTwister randomGen = new MersenneTwister(seed);
        List<Position2D> generatedPointsOnMap = randomPointsByRandomMapElementSelection(numberOfPoints, roadMap, randomGen);

        TMatrix centerMatrix = roadMap.center(0.0, 0.0);

        boolean withVisualisation = false;
        System.out.println("Show map? y|<other> and <enter>");
        String showMap = scanner.next();
        if(showMap.equalsIgnoreCase("y"))
        {
            float streetWidth = (float)1.5;
            float streetHeight = (float)0.4;
            
            VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, vrep, clientID, _objectCreator);
            mapCreator.createMeshBasedMap(roadMap);
            mapCreator.createMapSizedRectangle(roadMap, false);
            withVisualisation = true;
        }
        
        System.out.println("Show points? y|n and <enter>");
        String showPoints = scanner.next();
        if(showPoints.equalsIgnoreCase("y"))
        {
            visualiseInSimulator(roadMap, generatedPointsOnMap, centerMatrix);
            withVisualisation = true;
        }
        
        System.out.println("Check routes for sanity? Y and <enter> for yes, anything else and <enter> for no");
        String checkRoutes = scanner.next();
        if(checkRoutes.equalsIgnoreCase("y"))
        {
            List<ErrorMsg> errorMsg = checkRoutesSanity(roadMap, generatedPointsOnMap, centerMatrix);

            System.out.println("Show routes? y and <enter> to show all, anything else and <enter> to not show them");
            String showRoutes = scanner.next();
            if(showRoutes.equalsIgnoreCase("y"))
            {
                VRepNavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
                navigationListener.activateSegmentDebugging();
                
                errorMsg.stream().forEach(msg -> 
                {
                    navigationListener.setIdCreator(() -> String.format("%03d", msg.getIdx())); 
                    navigationListener.notifySegmentsChanged(msg.getTrajectory(), msg.curPos, msg.nexPos);
                });
                withVisualisation = true;
            }
        }
        
        System.out.println("n and <enter> to leave or a filename and <enter> to save the pointlist");
        String input2 = scanner.next();
        if(input2.equalsIgnoreCase("n"))
        {
            System.out.println("leaving");
        }
        else
        {
            System.out.println("saving to: " + input2);
            List<String> pointsAsStrings = generatedPointsOnMap.stream().map(curPos -> curPos.toFormattedString("%8.2f, %8.2f")).collect(Collectors.toList());
            try
            {
                Path targetPath = Paths.get(Paths.get("").toAbsolutePath().toString(), RES_ROADNETWORKS_DIRECTORY, input2);
                Files.write(targetPath, pointsAsStrings,Charset.defaultCharset());
            } 
            catch (IOException exc)
            {
                exc.printStackTrace();
            }
            System.out.println("done, leaving.");
        }
        scanner.close();
        if(withVisualisation)
        {
            System.out.println("cleaning visualisation");
            _objectCreator.deleteAll();
            _objectCreator.removeScriptloader();
            System.out.println("done");
        }
    }

    private static List<ErrorMsg> checkRoutesSanity(RoadMap map, List<Position2D> sumoPositions, TMatrix centerMatrix)
    {
        List<Position2D> positions = new ArrayList<Position2D>();
        sumoPositions.forEach(pos -> positions.add(new Position2D(pos)));
        
        positions.forEach(pos -> pos.transform(centerMatrix));
        List<PositionPair> pairs = new ArrayList<PositionPair>();
        for(int idx = 0; idx < positions.size() - 1; idx++)
        {
            Position2D curPos = positions.get(idx);
            Position2D nexPos = positions.get(idx + 1);
            PositionPair pair = new PositionPair(curPos, nexPos, idx);
            pairs.add(pair);
        }
        _numOfRoutes = pairs.size();
        List<ErrorMsg> errorMessages = pairs.parallelStream().map(pair -> checkSanityForPositionPair(map, pair.idx, pair.curPos, pair.nexPos)).collect(Collectors.toList());

        errorMessages.stream().forEach(msg -> System.out.println(msg.getLongMsg()));
        errorMessages.stream().forEach(msg -> System.out.print(msg.getShortMsg()));
        System.out.println("");
        int numOfErrors = errorMessages.stream().filter(msg -> msg.isFailed()).collect(Collectors.toList()).size();
        System.out.println("Number of erronous routes: " + numOfErrors);

        return errorMessages;
    }

    private static void visualiseInSimulator(RoadMap roadMap, List<Position2D> resultPointsOnMap, TMatrix centerMatrix) throws VRepException
    {
        ArrayList<Position2D> copy = new ArrayList<Position2D>();
        resultPointsOnMap.forEach(position -> copy.add(new Position2D(position)));
        copy.forEach(position -> position.transform(centerMatrix));

        for(int idx = 0; idx < copy.size(); idx++)
        {
            Position2D curPos = copy.get(idx);
            ShapeParameters params = new ShapeParameters();
            params.makeStandardSphere();
            String markerName = String.format("point_%03d", idx);
            params.setName(markerName);
            params.setPosition((float)curPos.getX(), (float)curPos.getY(), 0.1f);
            params.setSize(15.0f, 15.0f, 15.0f);
            _objectCreator.createPrimitive(params);
        }
    }

    private static List<Position2D> randomPointsByRandomMapElementSelection(int numberOfPoints, RoadMap roadMap, MersenneTwister randomGen)
    {
        List<Position2D> resultPointsOnMap = new ArrayList<Position2D>();
        List<EdgeType> edges = filterEdgesSuitableForPositions(roadMap);
        List<Line2D> lines = new ArrayList<Line2D>();
        System.out.println("gathering lines...");
        double minimumEdgeLength = 14.0;
        double minimumLineLength = 2.0;
        for(int edgeIdx = 0; edgeIdx < edges.size(); edgeIdx++)
        {
            EdgeType curEdge = edges.get(edgeIdx);
            List<LaneType> lanes = curEdge.getLane();
            double edgeLength = curEdge.getLength() == null ? curEdge.getLane().get(0).getLength() : curEdge.getLength();
            for(int laneIdx = 0; laneIdx < lanes.size(); laneIdx++)
            {
                LaneType curLane = lanes.get(laneIdx);
                List<Line2D> laneLines = Line2D.createLines(curLane.getShape());
                if(edgeLength < minimumEdgeLength)
                {
                    continue;
                }
                laneLines = laneLines.stream().filter(line -> line.length() > minimumLineLength).collect(Collectors.toList());
                lines.addAll(laneLines);
            }
        }
        System.out.println("selecting points on " + lines.size() + " lines...");
        int cnt = 0;
        int tries = 0;
        int maxTries = 100000;
        double minimumDistanceBetweenPoints = 400.0; //around 400 meters between any two points

        MersenneTwister distanceRandomGen = new MersenneTwister(50);
        while(cnt < numberOfPoints && tries < maxTries)
        {
            tries++;
            int randomLineIdx = randomGen.nextInt(lines.size());
            Line2D line = lines.get(randomLineIdx);
            Vector2D v = new Vector2D(line);
            
            double nextGaussian = randomGen.nextGaussian() / minimumLineLength;
            double randomVal = nextGaussian + 0.5;
            randomVal = setRange(randomVal, 0.01, 0.99);
            v.setLength(v.getLength() * randomVal);
            
            Position2D mapPoint = v.getTip();
            boolean isTooClose = checkDistanceToOtherPoints(resultPointsOnMap, minimumDistanceBetweenPoints, distanceRandomGen, mapPoint);
            if(isTooClose)
            {
                continue;
            }
            cnt++;
            resultPointsOnMap.add(mapPoint);
        }
        System.out.println("done");
        return resultPointsOnMap;
    }

    private static List<EdgeType> filterEdgesSuitableForPositions(RoadMap roadMap)
    {
        List<EdgeType> rawEdges = roadMap.getEdges();
        List<EdgeType> edges = rawEdges.stream().filter(curEdge -> (curEdge.getFunction() == null || !curEdge.getFunction().equalsIgnoreCase("internal"))).collect(Collectors.toList());
        Predicate<? super EdgeType> suitableEdge = 
                curEdge -> {
                    String edgeType = curEdge.getType();
                    return (edgeType != null && 
                            (
                                edgeType.equalsIgnoreCase("highway.residential") || 
                                edgeType.equalsIgnoreCase("highway.tertiary") ||
                                edgeType.equalsIgnoreCase("highway.unclassified") ||
                                edgeType.equalsIgnoreCase("highway.track")
                            )
                    );
                };
        edges = edges.stream().filter(suitableEdge).collect(Collectors.toList());
        return edges;
    }

    private static boolean checkDistanceToOtherPoints(List<Position2D> resultPointsOnMap,
            double minimumDistanceBetweenPoints, MersenneTwister distanceRandomGen, Position2D mapPoint)
    {
        boolean isTooClose = false;
        for(int idx = 0; idx < resultPointsOnMap.size(); idx++)
        {
            double curDist = Position2D.distance(mapPoint, resultPointsOnMap.get(idx));
            isTooClose = curDist < minimumDistanceBetweenPoints + distanceRandomGen.nextDouble() * 50.0;
            if(isTooClose)
            {
                break;
            }
        }
        return isTooClose;
    }

    private static double setRange(double randomVal, double lowerBound, double upperBound)
    {
        randomVal = randomVal <= lowerBound ? lowerBound : randomVal;
        randomVal = randomVal >= upperBound ? upperBound : randomVal;
        return randomVal;
    }
    
    private static ErrorMsg checkSanityForPositionPair(RoadMap map, int idx, Position2D curPos, Position2D nexPos) 
    {
        StringBuilder result = new StringBuilder();
        Navigator nav = new Navigator(map);
        DijkstraAlgo dijkstra = new DijkstraAlgo(map);
        System.out.format("Processing route from point number %d to point number %d\n", idx, idx + 1);
        result.append(String.format("> Checking route from point number %d to point number %d\n", idx, idx + 1));
        List<Line2D> route = nav.getRoute(curPos, nexPos);
        boolean failed = false;
        if(route == null)
        {
            result.append(String.format("     XX No route between these points!: (%.2f, %.2f), (%.2f, %.2f)\n", curPos.getX(), curPos.getY(), nexPos.getX(), nexPos.getY()));
            result.append(String.format("route: %d\n",  idx));
            failed = true;
        }
        else
        {
            EdgeType startEdge = map.getClosestEdgeFor(curPos);
            EdgeType targetEdge = map.getClosestEdgeFor(nexPos);
            JunctionType startJunction = map.getJunctionForName(startEdge.getTo());
            JunctionType targetJunction = map.getJunctionForName(targetEdge.getFrom());

            dijkstra.setSource(startJunction);
            dijkstra.setTarget(targetJunction);
            List<Node> path = dijkstra.getPath();
            if(path == null)
            {
                result.append("what? navigator found route, dijkstra didn't?");
                failed = true;
            }
            else
            {
                if(path.size() == 1)
                {
                    Node singleNode = path.get(0);
                    List<Edge> attachedEdges = new ArrayList<Edge>();
                    attachedEdges.addAll(singleNode.getOutgoingEdges());
                    attachedEdges.addAll(singleNode.getIncomingEdges());
                    EdgeType edgeCur = map.getClosestEdgeFor(curPos);
                    EdgeType edgeNex = map.getClosestEdgeFor(nexPos);
                    boolean nexIsAttached = false;
                    boolean curIsAttached = false;
                    for (Edge curAttached : attachedEdges)
                    {
                        if(curAttached.getSumoEdge() == edgeCur)
                        {
                            curIsAttached = true;
                        }
                        if(curAttached.getSumoEdge() == edgeNex)
                        {
                            nexIsAttached = true;
                        }
                    }
                    boolean nodeConnects = curIsAttached && nexIsAttached;
                    if(!nodeConnects)
                    {
                        result.append(String.format("     XX Just one node. Node does not connect these points: (%.2f, %.2f), (%.2f, %.2f)\n", curPos.getX(), curPos.getY(), nexPos.getX(), nexPos.getY()));
                        failed = true;
                    }
                }
                else
                {
                    for(int pathIdx = 0; pathIdx < path.size() - 1; pathIdx++)
                    {
                        Node curNode = path.get(pathIdx);
                        Node nexNode = path.get(pathIdx + 1);
                        Edge connectingEdge = curNode.getOutgoingEdge(nexNode);
                        if(connectingEdge == null)
                        {
                            result.append("what? navigator found route, dijkstra too but the nodes aren't connected!");
                            result.append(String.format("Strange found points with no connection!: (%.2f, %.2f), (%.2f, %.2f)\n", curPos.getX(), curPos.getY(), nexPos.getX(), nexPos.getY()));
                            result.append(String.format("route: %d\n",  idx));
                            failed = true;
                            break;
                        }
                    }
                }
            }
            result.append(String.format("     VVV route with %d lines found: \n", route.size()));
        }
        result.append("< Checked ");
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityFactory = segmentSize -> new NoVelocityAssigner();
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);
        List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(route);

        ErrorMsg resultMsg = new ErrorMsg(result.toString(), failed, idx, curPos, nexPos, trajectoryElements);
        _numOfRoutes--;
        System.out.println("Remaining routes to work on: " + _numOfRoutes);
        return resultMsg;
    }
}
