package de.joachim.haensel.phd.scenario.experiment.setup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
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

public class ShowPositionsInVrep
{
    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";
    private static int _routeCnt;

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

        System.out.println("n and <enter> to leave or a filename and <enter> to load a pointlist");
        String input2 = scanner.next();
        if(input2.equalsIgnoreCase("n"))
        {
            System.out.println("leaving");
        }
        else
        {
            RoadMap roadMap = new RoadMap(RES_ROADNETWORKS_DIRECTORY + mapFileName);

            List<Position2D> resultPointsOnMap = readPositionsFromFile(input2);

            VRepRemoteAPI vrep = VRepRemoteAPI.INSTANCE;
            int clientID = vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
            VRepObjectCreation objectCreator = new VRepObjectCreation(vrep, clientID);
            float streetWidth = (float)1.5;
            float streetHeight = (float)0.4;

            TMatrix centerMatrix = roadMap.center(0.0, 0.0);
            
            ArrayList<Position2D> copy = new ArrayList<Position2D>();
            resultPointsOnMap.forEach(position -> copy.add(new Position2D(position)));
            copy.forEach(position -> position.transform(centerMatrix));

            VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, vrep, clientID, objectCreator);
            mapCreator.createMeshBasedMap(roadMap);
            mapCreator.createMapSizedRectangle(roadMap, false);
            System.out.println("Show endpoints? (y and <enter> for yes, anything else and <enter> no");
            String showPoints = scanner.next();
            if(showPoints.equalsIgnoreCase("y"))
            {
                for(int idx = 0; idx < copy.size(); idx++)
                {
                    Position2D curPos = copy.get(idx);
                    ShapeParameters params = new ShapeParameters();
                    params.makeStandardSphere();
                    String markerName = String.format("point_%03d", idx);
                    params.setName(markerName);
                    params.setPosition((float)curPos.getX(), (float)curPos.getY(), 0.1f);
                    params.setSize(15.0f, 15.0f, 15.0f);
                    objectCreator.createPrimitive(params);
                }
                
                System.out.println("done");
            }
            System.out.println("Show routes? y and <enter> to show all, a comma seperated list of endpoints and <enter> for specific routes and anything else and <enter> to leave");
            String showRoutes = scanner.next();
            if(showRoutes.equalsIgnoreCase("y"))
            {
                List<PositionPair> pairs = new ArrayList<PositionPair>();
                for(int idx = 0; idx < copy.size() - 1; idx++)
                {
                    Position2D curPos = copy.get(idx);
                    Position2D nexPos = copy.get(idx + 1);
                    PositionPair pair = new PositionPair(curPos, nexPos, idx);
                    pairs.add(pair);
                }
                _routeCnt  = pairs.size();
                pairs.parallelStream().forEach(pair -> computeRoute(roadMap, pair));
                
                VRepNavigationListener navigationListener = new VRepNavigationListener(objectCreator);
                navigationListener.activateSegmentDebugging();

                pairs.stream().forEach(pair -> {navigationListener.setIdCreator(() -> String.format("%03d", pair.idx)); navigationListener.notifySegmentsChanged(pair.result, pair.curPos, pair.nexPos);});
            }
            else 
            {
                String[] pointNumbers = showRoutes.split(",");
                if(pointNumbers.length == 2)
                {
                    int idx1 = Integer.parseInt(pointNumbers[0]);
                    int idx2 = Integer.parseInt(pointNumbers[1]);
                    List<PositionPair> pairs = new ArrayList<PositionPair>();
                    if(idx1 < idx2)
                    {
                        for(int idx = idx1; idx < Math.min(idx2, copy.size() - 1); idx++)
                        {
                            Position2D curPos = copy.get(idx);
                            Position2D nexPos = copy.get(idx + 1);
                            PositionPair pair = new PositionPair(curPos, nexPos, idx);
                            pairs.add(pair);
                        }
                        _routeCnt  = pairs.size();
                        pairs.parallelStream().forEach(pair -> computeRoute(roadMap, pair));
                        
                        VRepNavigationListener navigationListener = new VRepNavigationListener(objectCreator);
                        navigationListener.activateSegmentDebugging();
                        
                        pairs.stream().forEach(pair -> {navigationListener.setIdCreator(() -> String.format("%03d", pair.idx)); navigationListener.notifySegmentsChanged(pair.result, pair.curPos, pair.nexPos);});
                    }
                }
            }
                
            System.out.println("type anything and <enter> to leave.");
            scanner.next();
            System.out.println("done, leaving.");
            System.out.println("cleaning visualisation");
            
            objectCreator.deleteAll();
            objectCreator.removeScriptloader();
            System.out.println("done");
        }
        scanner.close();
    }

    private static void computeRoute(RoadMap roadMap, PositionPair positionPair)
    {
        Navigator navigator = new Navigator(roadMap);
        ISegmenterFactory segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityFactory = segmentSize -> new NoVelocityAssigner();
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityFactory, 5.0);
        List<Line2D> route = navigator.getRoute(positionPair.curPos, positionPair.nexPos);
        List<TrajectoryElement> trajectoryElements = trajectorizer.createTrajectory(route);
        positionPair.result = trajectoryElements;
        _routeCnt--;
        System.out.println("routes to go: " + _routeCnt);
    }

    private static List<Position2D> readPositionsFromFile(String filename)
    {
        String completeRelativePath = RES_ROADNETWORKS_DIRECTORY + filename;
        try
        {
            List<String> pointsAsString = Files.readAllLines(new File(completeRelativePath).toPath());
            List<Position2D> allPositions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());
            return allPositions;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
