package de.joachim.haensel.phd.scenario.map;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import sumobindings.EdgeType;
import sumobindings.LaneType;

public class Edge implements IStreetSection
{
    private EdgeType _sumoEdge;

    public Edge(EdgeType sumoEdge)
    {
        _sumoEdge = sumoEdge;
    }

    public Float getLength()
    {
        LaneType firstLane = _sumoEdge.getLane().get(0);
        return firstLane.getLength();
    }

    public EdgeType getSumoEdge()
    {
        return _sumoEdge;
    }

    @Override
    public double getDistance(Position2D position)
    {
        double result = RoadMap.edgeToPointDistance(position, _sumoEdge);
        return result;
    }

    @Override
    public Vector2D getAPosition()
    {
        List<Line2D> shapeLines = Line2D.createLines(_sumoEdge.getLane().get(0).getShape());
        if(shapeLines != null && !shapeLines.isEmpty())
        {
            return new Vector2D(shapeLines.get(0));
        }
        else
        {
            return new Vector2D(0.0, 0.0, 0.0, 0.0);
        }
    }
}
