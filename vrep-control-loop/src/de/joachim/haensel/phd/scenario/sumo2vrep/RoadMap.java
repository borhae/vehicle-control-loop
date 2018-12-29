package de.joachim.haensel.phd.scenario.sumo2vrep;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;
import sumobindings.NetType;

public class RoadMap
{
    public class EdgeIntersection
    {
        private EdgeType _edge;
        private Line2D _line;
        private double _dist;
        private double _orientationDelta;

        public EdgeIntersection(EdgeType edge, Line2D line, double dist)
        {
            _edge = edge;
            _line = line;
            _dist = dist;
        }

        public void computeOrientationDelta(Vector2D orientation)
        {
            _orientationDelta = Vector2D.computeAngle(new Vector2D(_line), orientation);
        }

        public double getOrientationDelta()
        {
            return _orientationDelta;
        }

        public EdgeType getEdge()
        {
            return _edge;
        }

        @Override
        public String toString()
        {
            if(_line != null)
            {
                return _line.toString() + ", delta:" + _orientationDelta + ", dist:" + _dist;
            }
            return super.toString();
        }
    }

    private static final String INTERNAL_DESIGNATOR = "internal";
    private NetType _roadNetwork;
    private HashMap<JunctionType, Node> _navigableNetwork;

    private Map<String, JunctionType> _nameToJunctionMap;
    private Map<String, LaneType> _nameToLaneMap;
    private Map<String, EdgeType> _nameToEdgeMap;

    private Map<Position2D, LaneType> _positionToLaneMap;
    private TMatrix _transformationMatrix;


    public RoadMap(String networkFileName)
    {
        _roadNetwork = readSumoMap(networkFileName);
        _navigableNetwork = new HashMap<>();
        _nameToJunctionMap = new HashMap<>();
        _nameToLaneMap = new HashMap<>();
        _nameToEdgeMap = new HashMap<>();
        _positionToLaneMap = new HashMap<>();
        getJunctions().stream().forEach(junction -> {if(!isInternal(junction)){_nameToJunctionMap.put(junction.getId(), junction);}});
        getEdges().stream().forEach(edge -> {if(!isInternal(edge)){insertAllLanesFrom(edge); _nameToEdgeMap.put(edge.getId(), edge);}});
        createNavigableNetwork();
    }

    private boolean isInternal(EdgeType edge)
    {
        String function = edge.getFunction();
        return function != null && function.equals(INTERNAL_DESIGNATOR);
    }

    private boolean isInternal(JunctionType junction)
    {
        String type = junction.getType();
        return type != null && type.equals(INTERNAL_DESIGNATOR);
    }

    private void createNavigableNetwork()
    {
        createNodes();
        connectNodes();
    }
    
    private void createNodes()
    {
        List<JunctionType> junctions = _roadNetwork.getJunction();
        junctions.stream().forEach(junction -> _navigableNetwork.put(junction, new Node(junction)));
    }

    private void connectNodes()
    {
       _roadNetwork.getEdge().stream().forEach(edge -> createEdgeConnections(edge));
    }

    private void createEdgeConnections(EdgeType edge)
    {
        if(!(edge.getFunction() == null || edge.getFunction().isEmpty()))
        {
            return;
        }
        Edge navigableEdge = new Edge(edge);
        
        JunctionType fromJunction = _nameToJunctionMap.get(edge.getFrom());
        JunctionType toJunction = _nameToJunctionMap.get(edge.getTo());
        
        Node navigableFromJunction = _navigableNetwork.get(fromJunction);
        Node navigableToJunction = _navigableNetwork.get(toJunction);

        navigableFromJunction.addOutgoing(navigableEdge, navigableToJunction);
        navigableToJunction.addIncomming(navigableEdge, navigableFromJunction);
    }

    private void insertAllLanesFrom(EdgeType edge)
    {
        edge.getLane().stream().forEach(lane -> {_nameToLaneMap.put(lane.getId(), lane); insertAllPositions(lane);});
    }

    private void insertAllPositions(LaneType lane)
    {
        String shape = lane.getShape();
        String[] shapeCoordinates = shape.split(" ");
        for (String curCoordinate : shapeCoordinates)
        {
            Position2D newPosition = new Position2D(curCoordinate);
            _positionToLaneMap.put(newPosition, lane);
        }
    }
    
    private NetType readSumoMap(String networkFileName) 
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(NetType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            FileReader mapFileReader = new FileReader(new File(networkFileName));
            return (NetType) unmarshaller.unmarshal(mapFileReader);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List<JunctionType> getJunctions()
    {
        return _roadNetwork.getJunction();
    }
    
    public List<EdgeType> getEdges()
    {
        return _roadNetwork.getEdge();
    }

    public JunctionType getJunctionForName(String elemName)
    {
        return _nameToJunctionMap.get(elemName);
    }
    
    public EdgeType getEdgeForName(String edgeName)
    {
        return _nameToEdgeMap.get(edgeName);
    }
    
    public LaneType getLaneForName(String laneName)
    {
        return _nameToLaneMap.get(laneName);
    }

    public NetType getNetwork()
    {
        return _roadNetwork;
    }

    public OrientedPosition computeLaneEntryAtJunction(JunctionType junction, LaneType laneToHeadFor)
    {
        float junctionX = junction.getX();
        float junctionY = junction.getY();
        
        Position2D[] lineCoordinates = Position2D.valueOf(laneToHeadFor.getShape().split(" "));
        double minDist = Float.MAX_VALUE;
        int minIdx = -1;
        for (int idx = 0; idx < lineCoordinates.length; idx++)
        {
            Position2D curCoordinate = lineCoordinates[idx];
            double curDist = Point2D.distance(curCoordinate.getX(), curCoordinate.getY(), junctionX, junctionY);
            if(curDist < minDist)
            {
               minDist = curDist;
               minIdx = idx;
            }
        }
        Position2D startPos = lineCoordinates[minIdx];
        double angle = 0.0;
        // defined by two points
        if(lineCoordinates.length == 2)
        {
            int otherIdx = minIdx == 0 ? 1 : 0;
            Position2D destPos = lineCoordinates[otherIdx];
            angle = Math.atan2(startPos.getY() - destPos.getY(), startPos.getX() - destPos.getX()) + Math.PI / 2;
        }
        return new OrientedPosition(startPos, angle);
    }

    public LaneType getClosestLaneFor(Position2D position)
    {
        double smallestDist = Double.MAX_VALUE;
        double curDist = Double.MAX_VALUE;
        LaneType curClosest = null;
        Collection<LaneType> lanes = _nameToLaneMap.values();
        for (LaneType curLane : lanes)
        {
            curDist = computeSmallestLaneToPointDistance(position, curLane, null);
            if(curDist < smallestDist)
            {
                smallestDist = curDist;
                curClosest = curLane;
            }
        }
        return curClosest;
    }
    
    public EdgeType getClosestEdgeFor(Position2D position)
    {
        double smallestDist = Double.MAX_VALUE;
        double curDist = Double.MAX_VALUE;
        EdgeType curClosest = null;
        Collection<EdgeType> edges = _nameToEdgeMap.values();
        for (EdgeType curEdge : edges)
        {
            curDist = computeSmallestEdgeToPointDistance(position, curEdge);
            if(curDist < smallestDist)
            {
                smallestDist = curDist;
                curClosest = curEdge;
            }
        }
        return curClosest;
    }

    /**
     * Returns the edge that is closest to the point with the given orientation
     * (Internally edges are collected with respect to distance. The last 10 edges are filtered for the ones that 
     * have an orientation discrepancy of more than 90 degree. From this result the first and therefore closest is taken).
     * @param position
     * @param orientation
     * @return
     */
    public EdgeType getClosestEdgeForOrientationRestricted(Position2D position, Vector2D orientation)
    {
        double smallestDist = Double.MAX_VALUE;
        double curDist = Double.MAX_VALUE;
        EdgeType curClosest = null;
        Collection<EdgeType> edges = _nameToEdgeMap.values();
        Deque<EdgeIntersection> edgeIntersections = new ArrayDeque<>(); 
        for (EdgeType curEdge : edges)
        {
            Line2D resultLine = new Line2D(0.0, 0.0, 0.0, 0.0);
            curDist = computeSmallestEdgeToPointDistance(position, curEdge, resultLine);
            if(curDist < smallestDist)
            {
                smallestDist = curDist;
                curClosest = curEdge;
                EdgeIntersection curClosestEdgeIntersection = new EdgeIntersection(curEdge, resultLine, curDist);
                edgeIntersections.push(curClosestEdgeIntersection);
            }
        }
        if(!edgeIntersections.isEmpty())
        {
            List<EdgeIntersection> closestEdges = new ArrayList<>(edgeIntersections).subList(0, Math.min(10, edgeIntersections.size()) - 1);
            closestEdges.forEach(cur -> cur.computeOrientationDelta(orientation));
            closestEdges.stream().filter(cur -> cur.getOrientationDelta() < (Math.PI / 2.0));
            curClosest = closestEdges.get(0).getEdge();
        }
        return curClosest;
    }

    private double computeSmallestEdgeToPointDistance(Position2D position, EdgeType edge, Line2D resultLine)
    {
        List<LaneType> lanes = edge.getLane();
        double minDist = Double.POSITIVE_INFINITY;
        Position2D pointOnLane = new Position2D(0, 0);
        Line2D tmpResultLine = new Line2D(0.0, 0.0, 0.0, 0.0);
        for (LaneType curLane : lanes)
        {
            Line2D curMinLine = new Line2D(0.0, 0.0, 0.0, 0.0);
            double curDist = computeSmallestLaneToPointDistance(position, curLane, pointOnLane, curMinLine);
            if(curDist < minDist)
            {
                minDist = curDist;
                tmpResultLine = curMinLine;
            }
        }
        resultLine.setLine(tmpResultLine);
        return minDist;
    }

    private double computeSmallestEdgeToPointDistance(Position2D position, EdgeType edge)
    {
        List<LaneType> lanes = edge.getLane();
        double minDist = Double.POSITIVE_INFINITY;
        Position2D pointOnLane = new Position2D(0, 0);
        for (LaneType curLane : lanes)
        {
            double curDist = computeSmallestLaneToPointDistance(position, curLane, pointOnLane);
            if(curDist < minDist)
            {
                minDist = curDist;
            }
        }
        return minDist;
    }

    public Line2D getClosestLineFor(Position2D position)
    {
        LaneType closestLane = getClosestLaneFor(position);
        
        String[] coordinateList = closestLane.getShape().split(" ");
        List<Line2D> lines = createLines(coordinateList);
        double locallyMinimalDistance = Double.MAX_VALUE;
        Line2D locallyClosestLine = lines.get(0);
        for (Line2D curLine : lines)
        {
            double curDistance = curLine.distance(position);
            if(curDistance < locallyMinimalDistance)
            {
                locallyMinimalDistance = curDistance;
                locallyClosestLine = curLine;
            }
        }
        return locallyClosestLine;
    }

    
    public Position2D getClosestPointOnMap(Position2D position)
    {
        Position2D result = new Position2D(0.0, 0.0);
        double smallestDist = Double.MAX_VALUE;
        double curDist = Double.MAX_VALUE;
        
        Collection<LaneType> lanes = _nameToLaneMap.values();
        Position2D intermediateResult = new Position2D(0.0, 0.0);
        for (LaneType curLane : lanes)
        {
            Position2D curClosestPoint = new Position2D(0.0, 0.0);
            curDist = computeSmallestLaneToPointDistance(position, curLane, curClosestPoint);
            if(curDist < smallestDist)
            {
                smallestDist = curDist;
                computeSmallestLaneToPointDistance(position, curLane, curClosestPoint);
                intermediateResult = curClosestPoint;
            }
        }
        result.setXY(intermediateResult);
        return result;

//        JunctionType closestJunction = getClosestJunctionFor(position);
//        Node node = _navigableNetwork.get(closestJunction);
//        Collection<Edge> incomingEdges = node.getIncomingEdges();
//        List<LaneType> lanes = new ArrayList<>();
//        incomingEdges.forEach(edge -> lanes.addAll(edge.getSumoEdge().getLane()));
//        if(lanes.isEmpty())
//        {
//            return new Position2D(0.0, 0.0);
//        }
//        Line2D closestLine = null;
//
//        double minimalDistance = Double.MAX_VALUE;
//        for (LaneType curLane : lanes)
//        {
//            String[] coordinateList = curLane.getShape().split(" ");
//            List<Line2D> lines = createLines(coordinateList);
//            for (Line2D curLine : lines)
//            {
//                Vector2D v = new Vector2D(curLine);
//
//                double curDistance = v.unboundedDistance(position);
//                if(curDistance < minimalDistance)
//                {
//                    minimalDistance = curDistance;
//                    closestLine = curLine;
//                }
//            }
//        }
//        Vector2D closestLineAsVector = new Vector2D(closestLine);
//        Position2D intersectionPoint = Vector2D.getPerpendicularIntersection(closestLineAsVector, position);
//        if(intersectionPoint == null)
//        {
//            Position2D p1 = closestLine.getP1();
//            double distP1 = position.distance(p1);
//            Position2D p2 = closestLine.getP2();
//            double distP2 = position.distance(p2);
//            if(distP1 < distP2)
//            {
//                intersectionPoint = p1;
//            }
//            else
//            {
//                intersectionPoint = p2;
//            }
//        }
//        return intersectionPoint;
    }

    public JunctionType getClosestJunctionFor(Position2D currentPosition)
    {
        Collection<JunctionType> junctions = _nameToJunctionMap.values();
        Comparator<JunctionType> junctionComp = (j1, j2) -> Double.compare(junctionDist(j1, currentPosition), junctionDist(j2, currentPosition)); 
        return junctions.stream().min(junctionComp).get();
    }

    private double junctionDist(JunctionType junction, Position2D position)
    {
        return position.distance(junction.getX(), junction.getY());
    }


    /**
     * Smallest because lanes might have several segments
     * @param position
     * @param lane
     * @param resultIntersection return by value: this variable will contain the intersection point on the line or the closest endpoint on that line
     * @param resultLine return by value: this variable will contain the line on which the point intersects
     * @return the minimal distance between the lines in lane and the intersection point
     */
    private double computeSmallestLaneToPointDistance(Position2D position, LaneType lane, Position2D resultIntersection, Line2D resultLine)
    {
        String[] coordinateList = lane.getShape().split(" ");
        List<Line2D> lines = createLines(coordinateList);
        double minDist = Double.MAX_VALUE;
        Position2D tmpResultIntersection = new Position2D(0.0, 0.0);
        Line2D tmpResultLine = new Line2D(0.0, 0.0, 0.0, 0.0);
        for (Line2D curLine : lines)
        {
            double curDistance;
            Vector2D v = new Vector2D(curLine);
            Position2D intersectionPoint = Vector2D.getPerpendicularIntersection(v, position);
            if(intersectionPoint == null)
            {
                double p1Dist = position.distance(curLine.getP1());
                double p2Dist = position.distance(curLine.getP2());
                curDistance = Math.min(p1Dist, p2Dist);
                if(p1Dist < p2Dist)
                {
                    curDistance = p1Dist;
                    intersectionPoint = curLine.getP1();
                }
                else
                {
                    curDistance = p2Dist;
                    intersectionPoint = curLine.getP2();
                }
            }
            else
            {
                curDistance = position.distance(intersectionPoint);
            }
            if(curDistance < minDist)
            {
                minDist = curDistance;
                tmpResultIntersection = intersectionPoint;
                tmpResultLine = curLine;
            }
        }
        resultIntersection.setXY(tmpResultIntersection);
        resultLine.setLine(tmpResultLine);
        return minDist;
    }

    /**
     * Smallest because lanes might have several segments
     * @param position
     * @param lane
     * @param resultIntersection return by value: this variable will contain the intersection point on the line or the closest endpoint on that line
     * @return the minimal distance between the lines in lane and the intersection point
     */
    private double computeSmallestLaneToPointDistance(Position2D position, LaneType lane, Position2D resultIntersection)
    {
        Line2D resultLine = new Line2D(0.0, 0.0, 0.0, 0.0);
        return computeSmallestLaneToPointDistance(position, lane, resultIntersection, resultLine );
    }

    public static List<Line2D> createLines(String[] shapeCoordinates)
    {
        List<Line2D> result = new ArrayList<>();
        for(int idx = 0; idx < shapeCoordinates.length - 1; idx++)
        {
            result.add(new Line2D(shapeCoordinates[idx], shapeCoordinates[idx + 1]));
        }
        return result;
    }

    public Collection<Node> getNodes()
    {
        return _navigableNetwork.values();
    }

    public Node getNode(JunctionType junction)
    {
        return _navigableNetwork.get(junction);
    }

    public void transform(double scale, double offX, double offY)
    {
        _transformationMatrix = new TMatrix(scale, offX, offY);
        transform(_transformationMatrix);
    }

    /**
     * Transform this maps' base data according to matrix (scale and offset supported)
     * @param transformationMatrix
     */
    public void transform(TMatrix transformationMatrix)
    {
        List<JunctionType> junctions = getJunctions();
        List<EdgeType> edges = getEdges();
        
        junctions.forEach(junction -> transformJunction(junction, transformationMatrix));
        for(int idx = 0; idx < edges.size(); idx++)
        {
            EdgeType curEdge = edges.get(idx);
            transformEdge(curEdge, transformationMatrix);
        }
    }

    private void transformJunction(JunctionType junction, TMatrix transformationMatrix)
    {
        //ignore z-part since we only deal with 2d maps yet
        Position2D pos = new Position2D(junction.getX(), junction.getY());
        pos.transform(transformationMatrix);
        junction.setX((float) pos.getX());
        junction.setY((float) pos.getY());

        String junctionCustomShape = junction.getCustomShape();
        if(junctionCustomShape != null && !junctionCustomShape.isEmpty())
        {
            String transformed = transformStringCoordinateList(transformationMatrix, junctionCustomShape);
            junction.setCustomShape(transformed);
        }
        String junctionShape = junction.getShape();
        if(junctionShape != null && !junctionShape.isEmpty())
        {
            String transformed = transformStringCoordinateList(transformationMatrix, junctionShape);
            junction.setShape(transformed);
        }
    }
    
    private void transformEdge(EdgeType edge, TMatrix transformationMatrix)
    {
        String edgeShape = edge.getShape();
        String transformed = transformStringCoordinateList(transformationMatrix, edgeShape);
        edge.setShape(transformed);
        List<LaneType> lanes = edge.getLane();
        for (LaneType curLane : lanes)
        {
            String laneShape = curLane.getShape();
            if(laneShape != null && !laneShape.isEmpty())
            {
                Stream<Position2D> transformedPositions = transformIntoPosition2D(transformationMatrix, laneShape);
                List<Position2D> transPos = transformedPositions.collect(Collectors.toList());
                double newLength = 0.0;
                for(int idx = 0; idx < transPos.size() -1; idx++)
                {
                    newLength += Position2D.distance(transPos.get(idx), transPos.get(idx + 1));
                }
                curLane.setLength((float) newLength);
                curLane.setShape(position2DToString(transPos.stream()));
            }
            String laneCustomShape = curLane.getCustomShape();
            if(laneCustomShape != null && !laneCustomShape.isEmpty())
            {
                String transformedLaneCustomShape = transformStringCoordinateList(transformationMatrix, laneCustomShape);
                curLane.setShape(transformedLaneCustomShape);
            }
        }
        
    }
    
    private String transformStringCoordinateList(TMatrix transformationMatrix, String coordinates)
    {
        Stream<Position2D> transformedPositions = transformIntoPosition2D(transformationMatrix, coordinates);
        return position2DToString(transformedPositions);
    }

    private Stream<Position2D> transformIntoPosition2D(TMatrix transformationMatrix, String coordinates)
    {
        if(coordinates == null)
        {
            return Stream.empty();
        }
        else
        {
            Stream<String> stringCoordinates = Arrays.asList(coordinates.split(" ")).stream();
            Stream<Position2D> posCoordinates = stringCoordinates.map(coordinate -> new Position2D(coordinate));
            Stream<Position2D> transformedPositions = posCoordinates.map(position -> position.transform(transformationMatrix));
            return transformedPositions;
        }
    }

    private String position2DToString(Stream<Position2D> transformedPositions)
    {
        Stream<String> transformedAsString = transformedPositions.map(position -> position.toSumoString());
        return transformedAsString.collect(Collectors.joining(" "));
    }

    public TMatrix center(double centerX, double centerY)
    {
        XYMinMax dimensions = computeMapDimensions();
        double offX = centerX - dimensions.minX() - dimensions.distX()/2.0;
        double offY = centerY - dimensions.minY() - dimensions.distY()/2.0;
        TMatrix m = new TMatrix(1.0, offX, offY);
        transform(m);
        return m;
    }

    public XYMinMax computeMapDimensions()
    {
        XYMinMax minMax = new XYMinMax();
        List<JunctionType> junctions = getJunctions();
        for (JunctionType curJunction : junctions)
        {
            if(curJunction.getType().equals("internal"))
            {
                continue;
            }
            updateMinMax(curJunction, minMax);
        }
        List<EdgeType> edges = getEdges();
        for (EdgeType curEdge : edges)
        {
            String function = curEdge.getFunction();
            if(function == null || function.isEmpty())
            {
                List<LaneType> lanes = curEdge.getLane();
                for (LaneType curLane : lanes)
                {
                    String shape = curLane.getShape();
                    String[] lineCoordinates = shape.split(" ");
                    int numberCoordinates = lineCoordinates.length;
                    if(numberCoordinates == 2)
                    {
                        String p1 = lineCoordinates[0];
                        String p2 = lineCoordinates[1];
                        updateMinMax(curLane, p1, p2, minMax);              
                    }
                    else
                    {
                        updateMinMaxRecursive(curLane, Arrays.asList(lineCoordinates), minMax);
                    }
                }
            }
        }
        return minMax;
    }

    private void updateMinMax(JunctionType junction, XYMinMax minMax)
    {
        float xPos = junction.getX();
        float yPos = junction.getY();
        minMax.update(xPos, yPos);
    }
    
    private void updateMinMaxRecursive(LaneType curLane, List<String> lineCoordinates, XYMinMax minMax)
    {
        int listSize = lineCoordinates.size();
        if(listSize >= 2)
        {
            updateMinMax(curLane, lineCoordinates.get(0), lineCoordinates.get(1), minMax);
            if(listSize >= 3)
            {
                updateMinMaxRecursive(curLane, lineCoordinates.subList(1, lineCoordinates.size()), minMax);
            }
        }
    }

    private void updateMinMax(LaneType curLane, String p1, String p2, XYMinMax minMax)
    {
        String[] coordinate1 = p1.split(",");
        String[] coordinate2 = p2.split(",");
        
        float x1 = Float.parseFloat(coordinate1[0]);
        float y1 = Float.parseFloat(coordinate1[1]);
        minMax.update(x1, y1);
        
        float x2 = Float.parseFloat(coordinate2[0]);
        float y2 = Float.parseFloat(coordinate2[1]);
        minMax.update(x2, y2);
    }
}
