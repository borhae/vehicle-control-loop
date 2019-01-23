package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import sumobindings.EdgeType;

public class ObservationTuple
{
    private Position2D _rearWheelCP;
    private Position2D _frontWheelCP;
    private double[] _velocity;
    private List<IStreetSection> _viewAhead;
    private long _timeStamp;

    public ObservationTuple(Position2D rearWheelCP, Position2D frontWheelCP, double[] velocity, List<IStreetSection> viewAhead, long timeStamp)
    {
        _rearWheelCP = rearWheelCP;
        _frontWheelCP = frontWheelCP;
        _velocity = velocity;
        _viewAhead = viewAhead;
        _timeStamp = timeStamp;
    }

    public List<String> toPyplotString()
    {
        List<String> result = new ArrayList<>();
        result.add(new Line2D(_rearWheelCP, _frontWheelCP).toPyplotString("magenta"));
        Position2D vehicleCenter = new Position2D(Position2D.between(_rearWheelCP, _frontWheelCP));
        result.add(new Vector2D(vehicleCenter.getX(), vehicleCenter.getY(), _velocity[0], _velocity[1]).toLine().toPyplotString("red"));
        result.addAll(viewAheadToString(_viewAhead));
        return result;
    }

    private List<String> viewAheadToString(List<IStreetSection> viewAhead)
    {
        Function<IStreetSection, List<String>> sectionToString = section -> 
        {
            List<String> result = new ArrayList<>();
            //this is ugly, find a nicer solution later 
            if(section instanceof Node)
            {
                Node node = (Node)section;
                String shape = node.getShape();
                List<Position2D> nodePoints = Position2D.createPointList(shape);
                result.add(Position2D.toPyplotPolyline(nodePoints, "green"));
            }
            else if (section instanceof Edge)
            {
                Edge edge = (Edge) section;
                EdgeType sumoEdge = edge.getSumoEdge();
                result = Line2D.createLines(sumoEdge.getLane().get(0).getShape()).stream().map(line -> line.toPyplotString("gray")).collect(Collectors.toList());
            }
            return result;
        };
        List<String> result = viewAhead.stream().map(sectionToString).flatMap(Collection::stream).collect(Collectors.toList());
        return result;
    }
}
