package de.joachim.haensel.phd.scenario.experiment.setup;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.experiment.runner.ExperimentConfiguration;
import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.simulator.vrep.VRepSimulatorData;
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

public class ValidatedRoadPositionGeneratorConfigFile
{
    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";
    private VRepObjectCreation _objectCreator;
    private int _numOfRoutes;
    private String _mapFileName;
    private int _seed;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private boolean _visualisationInSimulator;
    private TMatrix _centerMatrix;
    private List<Position2D> _generatedPointsOnMap;
    private RoadMap _roadMap;
    private String _resultPath;
    private VRepSimulatorData _connectedSimulator;
    private ExperimentConfiguration _config;

    private boolean _positionsSaved;

    public ValidatedRoadPositionGeneratorConfigFile()
    {
        _positionsSaved = false;
    }
    
    public String getResultPath()
    {
        return _resultPath;
    }

    public boolean arePositionsSaved()
    {
        if(_config.getArePositionsToBeSaved())
        {
            return _positionsSaved;
        }
        else
        {
            return false;
        }
    }

    public void generateOriginDestinationList(ExperimentConfiguration config) throws VRepException
    {
        _config = config;
        _visualisationInSimulator = config.getVisualisationInSimulator();
        
        _mapFileName = config.getMapFileName();
        boolean chooseMap = !_mapFileName.equals("");
        if(!chooseMap)
        {
            _positionsSaved = false;
            return;
        }
        _seed = config.getSeed();
        _roadMap = new RoadMap(RES_ROADNETWORKS_DIRECTORY + _mapFileName);
        _generatedPointsOnMap = generateUnvalidatedPositions();
        _centerMatrix = _roadMap.center(0.0, 0.0);
        if(config.getShowMap())
        {
            showMap();
        }
        if(config.getShowStartEndPoints())
        {
            showRouteStartEndPoints();
        }
        List<ErrorMsg> errorMsgs = potentialSanityCheck(config);
        showRoutesInSimulator(config, errorMsgs);
        potentiallySaveToFile(config);
        
        if(_visualisationInSimulator)
        {
            System.out.println("cleaning visualisation");
            _objectCreator.deleteAll();
            if(config.getDetachSimulator())
            {
                _objectCreator.removeScriptloader();
                _vrep.simxFinish(_clientID);
            }
            System.out.println("done");
        }
        potentiallySaveToFile(config);
    }

    private void showRouteStartEndPoints() throws VRepException
    {
        if(!_visualisationInSimulator)
        {
            connectSimulator();
        }
        _visualisationInSimulator = true;
        visualiseInSimulator(_roadMap, _generatedPointsOnMap, _centerMatrix);
    }

    private void showMap() throws VRepException
    {
        if(!_visualisationInSimulator)
        {
            connectSimulator();
        }
        _visualisationInSimulator = true;
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(_roadMap);
        mapCreator.createMapSizedRectangle(_roadMap, false);
    }

    private void potentiallySaveToFile(ExperimentConfiguration config)
    {
        _resultPath = "";
        if(!config.getArePositionsToBeSaved())
        {
            _positionsSaved = false;
        }
        else
        {
            if(config.getPositionListFilenameDefault())
            {
                String city = getCityName();
                _resultPath = city.substring(0, 1).toUpperCase() + city.substring(1) + "points_generatedSeed" + getSeed() + ".txt";
            }
            else
            {
                _resultPath = config.getPositionListFileName();
            }
            List<String> pointsAsStrings = _generatedPointsOnMap.stream().map(curPos -> curPos.toFormattedString("%8.2f, %8.2f")).collect(Collectors.toList());
            System.out.println("saving to: " + _resultPath);
            try
            {
                Path targetPath = Paths.get(Paths.get("").toAbsolutePath().toString(), RES_ROADNETWORKS_DIRECTORY, _resultPath);
                Files.write(targetPath, pointsAsStrings,Charset.defaultCharset());
                _positionsSaved = true;
            } 
            catch (IOException exc)
            {
                _positionsSaved = false;
                exc.printStackTrace();
            }
        }
    }

    private void showRoutesInSimulator(ExperimentConfiguration config, List<ErrorMsg> errorMsgs) throws VRepException
    {
//        System.out.println("Show routes? y and <enter> to show all, anything else and <enter> to not show them");
//        String showRoutes = config.next();
//        if(showRoutes.equalsIgnoreCase("y"))
        if(config.getShowRoutes())
        {
            if(!_visualisationInSimulator)
            {
                connectSimulator();
            }
            _visualisationInSimulator = true;
            VRepNavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
            navigationListener.activateSegmentDebugging();
            
            errorMsgs.stream().forEach(msg -> 
            {
                navigationListener.setIdCreator(() -> String.format("%03d", msg.getIdx())); 
                navigationListener.notifySegmentsChanged(msg.getTrajectory(), msg.curPos, msg.nexPos);
            });
        }
    }

    private List<ErrorMsg> potentialSanityCheck(ExperimentConfiguration config)
    {
        List<ErrorMsg> errorMsg = null;
        if(config.getShouldRunRouteSanityCheck())
        {
            errorMsg = checkRoutesSanity(_roadMap, _generatedPointsOnMap, _centerMatrix);
        }
        return errorMsg;
    }

    private void connectSimulator() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
        _connectedSimulator = new VRepSimulatorData(_vrep, _clientID, _objectCreator);
    }

    private List<Position2D> generateUnvalidatedPositions()
    {
        MersenneTwister randomGen = new MersenneTwister(_seed);
        List<Position2D> generatedPointsOnMap = randomPointsByRandomMapElementSelection(_config.getNumberOfTargets(), _roadMap, randomGen);
        return generatedPointsOnMap;
    }

    private List<ErrorMsg> checkRoutesSanity(RoadMap map, List<Position2D> sumoPositions, TMatrix centerMatrix)
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

    private void visualiseInSimulator(RoadMap roadMap, List<Position2D> resultPointsOnMap, TMatrix centerMatrix) throws VRepException
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

    private List<Position2D> randomPointsByRandomMapElementSelection(int numberOfPoints, RoadMap roadMap, MersenneTwister randomGen)
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
        double minimumDistanceBetweenPoints = 300.0; //around 300 meters between any two points

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

    private List<EdgeType> filterEdgesSuitableForPositions(RoadMap roadMap)
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

    private boolean checkDistanceToOtherPoints(List<Position2D> resultPointsOnMap, double minimumDistanceBetweenPoints, MersenneTwister distanceRandomGen, Position2D mapPoint)
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

    private double setRange(double randomVal, double lowerBound, double upperBound)
    {
        randomVal = randomVal <= lowerBound ? lowerBound : randomVal;
        randomVal = randomVal >= upperBound ? upperBound : randomVal;
        return randomVal;
    }
    
    private ErrorMsg checkSanityForPositionPair(RoadMap map, int idx, Position2D curPos, Position2D nexPos) 
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

    public VRepSimulatorData getSimulatorData()
    {
        return _connectedSimulator;
    }

    public boolean simulatorConnected()
    {
        return _visualisationInSimulator;
    }

    public String getMapFileName()
    {
        return _mapFileName;
    }

    public String getCityName()
    {
        if(_mapFileName.equals("luebeck-roads.net.xml"))
        {
            return "luebeck";
        }
        else if(_mapFileName.equals("chandigarh-roads-lefthand.removed.net.xml"))
        {
            return "chandigarh";
        }
        else
        {
            return "";
        }
    }

    public int getNumberOfPositions()
    {
        return _config.getNumberOfTargets();
    }

    public long getSeed()
    {
        return _seed;
    }
}
