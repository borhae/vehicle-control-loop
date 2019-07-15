package de.joachim.haensel.phd.scenario.experiment.setup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DijkstraAlgo;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import sumobindings.EdgeType;

public class RouteSanityCheck
{
    public static void main(String[] args)
    {
        String networkName = 
                "luebeck-roads.net.xml";
//                "chandigarh-roads-lefthand.removed.net.xml";
        String pointsFileName = 
//                "Luebeckpoints_spread.txt";
                "Luebeckpoints_generatedSeed4096.txt";

        RoadMap map = new RoadMap("./res/roadnetworks/" + networkName);
        try (Stream<String> stream = Files.lines(Paths.get("./res/roadnetworks/" + pointsFileName))) 
        {
            List<Position2D> positions = stream.map(stringPos -> new Position2D(stringPos)).collect(Collectors.toList());
            List<PositionPair> pairs = new ArrayList<PositionPair>();
            for(int idx = 0; idx < positions.size() - 1; idx++)
            {
                Position2D curPos = positions.get(idx);
                Position2D nexPos = positions.get(idx + 1);
                PositionPair pair = new PositionPair(curPos, nexPos, idx);
                pairs.add(pair);
            }
            List<ErrorMsg> errorMsg = pairs.parallelStream().map(pair -> checkSanityForPositionPair(map, pair.idx, pair.curPos, pair.nexPos)).collect(Collectors.toList());

            errorMsg.stream().forEach(msg -> System.out.println(msg.getLongMsg()));
            errorMsg.stream().forEach(msg -> System.out.print(msg.getShortMsg()));
            System.out.println("");
            int numOfErrors = errorMsg.stream().filter(msg -> msg.isFailed()).collect(Collectors.toList()).size();
            System.out.println("Number of erronous routes: " + numOfErrors);
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
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
		    dijkstra.setSource(map.getClosestJunctionFor(curPos));
		    dijkstra.setTarget(map.getClosestJunctionFor(nexPos));
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
		ErrorMsg resultMsg = new ErrorMsg(result.toString(), failed, idx);
		return resultMsg;
	}
}
