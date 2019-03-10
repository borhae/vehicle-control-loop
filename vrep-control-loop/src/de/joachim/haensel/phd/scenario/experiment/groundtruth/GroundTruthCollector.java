package de.joachim.haensel.phd.scenario.experiment.groundtruth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.experiment.groundtruth.GroundTruthCollector.Direction;
import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;
import sumobindings.EdgeType;

public class GroundTruthCollector
{
    public enum Direction
    {
        FORWARD, BACKWARD

    }

    private RoadMap _map;
    private IVelocityAssignerFactory _velocityAssignerFactory;
    private ISegmenterFactory _segmenterFactory;

    public GroundTruthCollector(RoadMap map)
    {
        _map = map;
        _velocityAssignerFactory = new IVelocityAssignerFactory() {
            
            @Override
            public IVelocityAssigner create(double segmentSize)
            {
                BasicVelocityAssigner va = new BasicVelocityAssigner(segmentSize, UnitConverter.kilometersPerHourToMetersPerSecond(120.0), 3.8, 4.0, 1.0);
                va.setInitialVelocity(UnitConverter.kilometersPerHourToMetersPerSecond(50));
                return va;
            }
        };
        _segmenterFactory = segmentSize -> new Segmenter(segmentSize, new InterpolationSegmenterCircleIntersection());
    }
    
    private void go()
    {
        HashSet<Edge> edgesToCover = new HashSet<Edge>(_map.getNavigableEdges());
        List<Node> nodes = new ArrayList<Node>(_map.getNodes());
        List<List<TrajectoryElement>> plannedTrajectories = new ArrayList<List<TrajectoryElement>>();
        int nodeCountDown = nodes.size();
        for (Node curNode : nodes)
        {
//            System.out.print(">");
            Collection<Node> inNodes = curNode.getIncomingNodes();
            for (Node curIn : inNodes)
            {
                Edge curEdge = curIn.getOutgoingEdge(curNode);
                edgesToCover.remove(curEdge);
                List<List<TrajectoryElement>> routeTrajectories = createMiniRoute(curIn, curNode); // should be three
                plannedTrajectories.addAll(routeTrajectories);
            }
//            System.out.println("<");
            nodeCountDown--;
            System.out.format("nodes to cover: %d, edges to cover: %d\n", nodeCountDown, edgesToCover.size());
        }
        if(edgesToCover.isEmpty())
        {
            System.out.println("all done");
        }
        System.out.println("done either ways");
    }

    private List<List<TrajectoryElement>> createMiniRoute(Node source, Node target)
    {
        List<Node> path = new ArrayList<Node>(Arrays.asList(source, target));

        double totalLength = 145; // one window (20 * 5 = 100 Meter) plus three stepsizes (3 * 15 = 45 Meter) (sizes as in the simualtion)
        double after = 50;
        double firstLength = source.distance(target);
        double before = totalLength - after - firstLength;
        createMinSizePath(path, before, Direction.BACKWARD);
        createMinSizePath(path, after, Direction.FORWARD);
        boolean notValid = path.parallelStream().anyMatch(e -> !e.isValid());
        if(notValid)
        {
            System.out.println("couldn't create path for tuple: " + source.toString() + ", " + target.toString());
            return new ArrayList<List<TrajectoryElement>>();
        }
        Navigator navigator = new Navigator(_map);
        ITrajectorizer trajectorizer = new Trajectorizer(_segmenterFactory, _velocityAssignerFactory , 5.0);
        
        EdgeType firstEdge = path.get(0).getOutgoingEdge(path.get(1)).getSumoEdge();
        EdgeType lastEdge = path.get(path.size() - 2).getOutgoingEdge(path.get(path.size() -1)).getSumoEdge();
        List<Line2D> routeBasis = navigator.createLinesFromPath(path, firstEdge, lastEdge);
        List<TrajectoryElement> allSegments = trajectorizer.createTrajectory(routeBasis);
        return buildWindows(3, 20, allSegments);
    }

    private void createMinSizePath(List<Node> path, double requiredDistance, Direction dir)
    {
        if(requiredDistance > 0.0)
        {
            List<Node> nextNodes = new ArrayList<Node>();
            Node reference = null;
            if(dir == Direction.FORWARD)
            {
                Node last = path.get(path.size() - 1);
                Collection<Node> outNodes = last.getOutgoingNodes();
                nextNodes.addAll(outNodes);
                reference = last;
            }
            else if(dir == Direction.BACKWARD)
            {
                Node first = path.get(0);
                Collection<Node> inNodes = first.getIncomingNodes();
                nextNodes.addAll(inNodes);
                reference = first;
            }
            //maybe take a random one instead of the first one in the future
            if(reference != null && !nextNodes.isEmpty())
            {
                Node next = nextNodes.get(0);
                Float distance = reference.distance(next);
                if(dir == Direction.FORWARD)
                {
                    path.add(next);
                }
                else if(dir == Direction.BACKWARD)
                {
                    path.add(0, next);
                }
                if(distance > 0.0)
                {
                    createMinSizePath(path, requiredDistance - distance, dir);
                }
                else
                {
                    path.add(new Node(null));
                }
            }
            else
            {
                path.add(new Node(null));
            }
        }
    }

    private List<List<TrajectoryElement>> buildWindows(int stepSize, int numberOfElements, List<TrajectoryElement> allSegments)
    {
        List<List<TrajectoryElement>> result = new ArrayList<List<TrajectoryElement>>();
        int startIdx = 0;
        int endIdx = 20;
        for(; endIdx < allSegments.size(); startIdx += 15, endIdx += 15)
        {
            result.add(allSegments.subList(startIdx, endIdx));
        }
        return result;
    }

    public static void main(String[] args)
    {
        String mapFilename = args[0];
        RoadMap map = new RoadMap(mapFilename);
        GroundTruthCollector traverser = new GroundTruthCollector(map);
        traverser.go();
    }
}
