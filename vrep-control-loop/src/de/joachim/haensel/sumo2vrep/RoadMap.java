package de.joachim.haensel.sumo2vrep;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;
import sumobindings.NetType;

public class RoadMap
{
    private NetType _roadNetwork;
    private HashMap<JunctionType, Node> _navigableNetwork;

    private Map<String, JunctionType> _nameToJunctionMap;
    private Map<String, LaneType> _nameToLaneMap;
    private Map<String, EdgeType> _nameToEdgeMap;

    private Map<Position2D, LaneType> _positionToLaneMap;


    public RoadMap(String networkFileName)
    {
        _roadNetwork = readSumoMap(networkFileName);
        _navigableNetwork = new HashMap<>();
        _nameToJunctionMap = new HashMap<>();
        _nameToLaneMap = new HashMap<>();
        _nameToEdgeMap = new HashMap<>();
        _positionToLaneMap = new HashMap<>();
        getJunctions().stream().forEach(junction -> _nameToJunctionMap.put(junction.getId(), junction));
        getEdges().stream().forEach(edge -> {insertAllLanesFrom(edge); _nameToEdgeMap.put(edge.getId(), edge);});
        createNavigableNetwork();
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
        float smallestDist = Float.MAX_VALUE;
        float curDist = Float.MAX_VALUE;
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
        Comparator<JunctionType> junctionComp = (j1, j2) -> Float.compare(junctionDist(j1, currentPosition), junctionDist(j2, currentPosition)); 
        return junctions.stream().min(junctionComp).get();
    }

    private float junctionDist(JunctionType junction, Position2D position)
    {
        return position.distance(junction.getX(), junction.getY());
    }

    /**
     * Smallest because lanes might have several segments
     * @param position
     * @param lane
     * @return
     */
    private float computeSmallestLaneToPointDistance(Position2D position, LaneType lane)
    {
        String[] coordinateList = lane.getShape().split(" ");
        if(coordinateList.length == 2)
        {
            return new Segment(coordinateList[0], coordinateList[1]).distance(position);
        }
        List<Segment> segments = createSegments(coordinateList);
        float minDist = Float.MAX_VALUE;
        for (Segment curSegment : segments)
        {
            float curDistance = curSegment.distance(position);
            if(curDistance < minDist)
            {
                minDist = curDistance;
            }
        }
        return minDist;
    }

    private List<Segment> createSegments(String[] shapeCoordinates)
    {
        List<Segment> result = new ArrayList<>();
        for(int idx = 0; idx < shapeCoordinates.length - 1; idx+=2)
        {
            result.add(new Segment(shapeCoordinates[idx], shapeCoordinates[idx + 1]));
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
}
