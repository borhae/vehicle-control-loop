package de.joachim.haensel.sumo2vrep;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;
import sumobindings.NetType;

public class RoadMap
{
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
            curDist = computeSmallestLaneToPointDistance(position, curLane);
            if(curDist < smallestDist)
            {
                smallestDist = curDist;
                curClosest = curLane;
            }
        }
        return curClosest;
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
     * @return
     */
    private double computeSmallestLaneToPointDistance(Position2D position, LaneType lane)
    {
        String[] coordinateList = lane.getShape().split(" ");
        if(coordinateList.length == 2)
        {
            return new Line2D(coordinateList[0], coordinateList[1]).distance(position);
        }
        List<Line2D> lines = createLines(coordinateList);
        double minDist = Double.MAX_VALUE;
        for (Line2D curLine : lines)
        {
            double curDistance = curLine.distance(position);
            if(curDistance < minDist)
            {
                minDist = curDistance;
            }
        }
        return minDist;
    }

    private List<Line2D> createLines(String[] shapeCoordinates)
    {
        List<Line2D> result = new ArrayList<>();
        for(int idx = 0; idx < shapeCoordinates.length - 1; idx+=2)
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

    public void transform(TMatrix transformationMatrix)
    {
        List<JunctionType> junctions = getJunctions();
        List<EdgeType> edges = getEdges();
        
        junctions.forEach(junction -> transformJunction(junction, transformationMatrix));
        edges.forEach(edge -> transformEdge(edge, transformationMatrix));
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
                // TODO continue here
//                transformedPositions.
                curLane.setShape(position2DToString(transformedPositions));
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

    private Stream<Position2D> transformIntoPosition2D(TMatrix transformationMatrix, String laneShape)
    {
        Stream<String> stringCoordinates = Arrays.asList(laneShape.split(" ")).stream();
        Stream<Position2D> posCoordinates = stringCoordinates.map(coordinate -> new Position2D(coordinate));
        Stream<Position2D> transformedPositions = posCoordinates.map(position -> position.transform(transformationMatrix));
        return transformedPositions;
    }

    private String position2DToString(Stream<Position2D> transformedPositions)
    {
        Stream<String> transformedAsString = transformedPositions.map(position -> position.toSumoString());
        return transformedAsString.collect(Collectors.joining());
    }
}
