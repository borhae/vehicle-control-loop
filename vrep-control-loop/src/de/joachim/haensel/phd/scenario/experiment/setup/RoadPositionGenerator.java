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
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import sumobindings.EdgeType;
import sumobindings.LaneType;

/**
 * Generates points on given map. Does not check whether they are connected
 * @author dummy
 *
 */
public class RoadPositionGenerator
{
    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";
    private static VRepObjectCreation _objectCreator;

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
        
        MersenneTwister randomGen = new MersenneTwister(4096);
        List<Position2D> resultPointsOnMap = randomPointsByRandomMapElementSelection(numberOfPoints, roadMap, randomGen);

        System.out.println("Show points? y|n and <enter>");
        String input1 = scanner.next();
        boolean withVisualisation = false;
        if(input1.equalsIgnoreCase("y"))
        {
            visualiseInSimulator(roadMap, resultPointsOnMap);
            withVisualisation = true;
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
            List<String> pointsAsStrings = resultPointsOnMap.stream().map(curPos -> curPos.toFormattedString("%8.2f, %8.2f")).collect(Collectors.toList());
            try
            {
                Path targetPath = Paths.get(Paths.get("").toAbsolutePath().toString(), input2 + ".txt");
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

    private static void visualiseInSimulator(RoadMap roadMap, List<Position2D> resultPointsOnMap) throws VRepException
    {
        VRepRemoteAPI vrep = VRepRemoteAPI.INSTANCE;
        int clientID = vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(vrep, clientID);
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;

        TMatrix centerMatrix = roadMap.center(0.0, 0.0);
        
        ArrayList<Position2D> copy = new ArrayList<Position2D>();
        resultPointsOnMap.forEach(position -> copy.add(new Position2D(position)));
        copy.forEach(position -> position.transform(centerMatrix));

        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, vrep, clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
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
        Predicate<? super EdgeType> predicate = 
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
        edges = edges.stream().filter(predicate).collect(Collectors.toList());
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
}
