package de.joachim.haensel.phd.scenario.mapgenerator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;
import sumobindings.NetType;
import sumobindings.ObjectFactory;

public class MapGenerator
{
    public enum MapIDCreator
    {
        INSTANCE;
        int _junctionCnt = 0;
        int _edgeCnt = 0;
        Map<String, Integer> _edgeLaneCnt = new HashMap<>(); 
        
        public String getNextJunctionID()
        {
            String result = String.format("gneJ%d", _junctionCnt);
            _junctionCnt++;
            return result;
        }

        public String getNextEdgeID()
        {
            String result = String.format("gnE%d", _edgeCnt);
            _edgeCnt++;
            return result;
        }

        public String nextLaneID(EdgeType edge)
        {
            String edgeID = edge.getId();
            Integer laneCnt = _edgeLaneCnt.get(edgeID);
            if(laneCnt == null)
            {
                laneCnt = 0;
                _edgeLaneCnt.put(edgeID, laneCnt);
            }
            String result = String.format(edgeID + "_%d", laneCnt);
            _edgeLaneCnt.put(edgeID, laneCnt++);
            return result;
        }
    }

    public RoadMap generateMap(List<IArcsSegmentContainerElement> sourceFromProfile)
    {
        if(sourceFromProfile.isEmpty())
        {
            return null;
        }
        ObjectFactory objectFactory = new ObjectFactory();
        NetType netType = objectFactory.createNetType();
        netType.setVersion("0.27");

        IArcsSegmentContainerElement firstElem = sourceFromProfile.get(0);
        IArcsSegmentContainerElement lastElem = sourceFromProfile.get(sourceFromProfile.size() - 1);
        Position2D startPos = firstElem.getStart();
        Position2D endPos = lastElem.getEnd();

        JunctionType startJunction = createJunction(objectFactory, startPos);
        JunctionType endJunction = createJunction(objectFactory, endPos);
        
        EdgeType edge = createEdge(objectFactory, startJunction, endJunction);
        LaneType lane = createLane(sourceFromProfile, objectFactory, edge);
        edge.getLane().add(lane);
        
        
        netType.getJunction().add(startJunction);
        netType.getJunction().add(endJunction);
        netType.getEdge().add(edge);
        RoadMap map = new RoadMap(netType);
        return map;
    }

    private LaneType createLane(List<IArcsSegmentContainerElement> sourceFromProfile, ObjectFactory objectFactory, EdgeType edge)
    {
        LaneType lane = objectFactory.createLaneType();
        lane.setId(MapIDCreator.INSTANCE.nextLaneID(edge));
        lane.setIndex(BigInteger.valueOf(0l));
        lane.setSpeed(13.89f);
        List<Line2D> lines = extractLines(sourceFromProfile);
        double length = lines.stream().collect(Collectors.summingDouble(line -> line.length()));
        lane.setLength((float)length);
        String shape = posToShape(lines.stream().flatMap(line -> Stream.of(line.getP1(), line.getP2())));
        lane.setShape(shape);
        return lane;
    }

    private List<Line2D> extractLines(List<IArcsSegmentContainerElement> sourceFromProfile)
    {
        
        List<Line2D> lines = sourceFromProfile.stream().flatMap(val -> val.getLines().stream()).collect(Collectors.toList());
        return lines;
    }

    private EdgeType createEdge(ObjectFactory objectFactory, JunctionType from, JunctionType to)
    {
        EdgeType edge = objectFactory.createEdgeType();
        edge.setFrom(from.getId());
        edge.setTo(to.getId());
        edge.setId(MapIDCreator.INSTANCE.getNextEdgeID());
        return edge;
    }

    private JunctionType createJunction(ObjectFactory objectFactory, Position2D pos)
    {
        JunctionType junction = objectFactory.createJunctionType();
        junction.setId(MapIDCreator.INSTANCE.getNextJunctionID());
        junction.setType("");
        junction.setX((float)pos.getX());
        junction.setY((float) pos.getY());
        junction.setIncLanes("");
        junction.setShape(createJunctionShape(pos));
        return junction;
    }

    private String createJunctionShape(Position2D junctionCenter)
    {
        List<Position2D> rectangle = createRectangle();
        TMatrix mat = new TMatrix(1.0, junctionCenter.getX(), junctionCenter.getY());
        rectangle = rectangle.stream().map(val -> val.transform(mat)).collect(Collectors.toList());
//        return rectangle.stream().map(pos1 -> String.format("%.6f,%.6f", pos1.getX(), pos1.getY())).reduce("", (x, y) -> x + " " + y);
        return posToShape(rectangle.stream());
    }

    private String posToShape(Stream<Position2D> positions)
    {
        return positions.map(pos -> String.format("%.6f,%.6f", pos.getX(), pos.getY())).collect(Collectors.joining(" "));
    }

    private List<Position2D> createRectangle()
    {
        double[][] rectangle = new double[][]{
                {0.0, 0.0}, 
                {5.0, 0.0}, 
                {5.0, 5.0}, 
                {0.0, 5.0}
        };
        return Arrays.stream(rectangle).map(val -> new Position2D(val[0], val[1])).collect(Collectors.toList());
    }
}
