package de.joachim.haensel.phd.scenario.profile.equivalenceclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import sumobindings.EdgeType;

/**
 * TODO: view ahead is not saved
 * @author dummy
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ObservationTuple implements Comparable<ObservationTuple>
{
    private Position2D _rearWheelCP;
    private Position2D _frontWheelCP;

    private double[] _velocity;
//    private List<IStreetSection> _viewAhead;
    private long _timeStamp;
    private Vector2D _orientation;

    public ObservationTuple()
    {
    }
    
    public ObservationTuple(Position2D rearWheelCP, Position2D frontWheelCP, double[] velocity, long timeStamp)
    {
        _rearWheelCP = rearWheelCP;
        _frontWheelCP = frontWheelCP;
        _velocity = velocity;
        _timeStamp = timeStamp;
    }
    
//    public ObservationTuple(Position2D rearWheelCP, Position2D frontWheelCP, double[] velocity, List<IStreetSection> viewAhead, long timeStamp)
//    {
//        _rearWheelCP = rearWheelCP;
//        _frontWheelCP = frontWheelCP;
//        _velocity = velocity;
//        _viewAhead = viewAhead;
//        _timeStamp = timeStamp;
//    }

    public List<String> toPyplotString()
    {
        List<String> result = new ArrayList<>();
        result.add(new Line2D(_rearWheelCP, _frontWheelCP).toPyplotString("magenta"));
        Position2D vehicleCenter = new Position2D(Position2D.between(_rearWheelCP, _frontWheelCP));
        result.add(new Vector2D(vehicleCenter.getX(), vehicleCenter.getY(), _velocity[0], _velocity[1]).toLine().toPyplotString("red"));
//        result.addAll(viewAheadToString(_viewAhead));
        return result;
    }
    
    public List<String> toPyplotStringNoView()
    {
        List<String> result = new ArrayList<>();
        result.add(new Line2D(_rearWheelCP, _frontWheelCP).toPyplotString("magenta"));
        Position2D vehicleCenter = new Position2D(Position2D.between(_rearWheelCP, _frontWheelCP));
        result.add(new Vector2D(vehicleCenter.getX(), vehicleCenter.getY(), _velocity[0], _velocity[1]).toLine().toPyplotString("red"));
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

    public void transform(Position2D base, TMatrix rotationMatrix)
    {
        _rearWheelCP = Position2D.minus(_rearWheelCP, base);
        _frontWheelCP = Position2D.minus(_frontWheelCP, base);
        _rearWheelCP.transform(rotationMatrix);
        _frontWheelCP.transform(rotationMatrix);

        Vector2D transformedVel = new Vector2D(0.0, 0.0, _velocity[0], _velocity[1]).transform(rotationMatrix);
        Position2D velDir = transformedVel.getDir();
        _velocity[0] = velDir.getX();
        _velocity[1] = velDir.getY();
        //TODO modify view ahead if you have the time... (never?)
    }

    public Position2D getRearWheelCP()
    {
        return _rearWheelCP;
    }

    public Position2D getFrontWheelCP()
    {
        return _frontWheelCP;
    }

    public double[] getVelocity()
    {
        return _velocity;
    }

//    public List<IStreetSection> getViewAhead()
//    {
//        return _viewAhead;
//    }

    public long getTimeStamp()
    {
        return _timeStamp;
    }

    @Override
    public int compareTo(ObservationTuple other)
    {
        int orXComp = Double.compare(getOrientation().getNormX(), other.getOrientation().getNormX());
        if(orXComp == 0)
        {
            int orYComp = Double.compare(getOrientation().getNormY(), other.getOrientation().getbY());
            if(orYComp == 0)
            {
                int velXComp = Double.compare(_velocity[0], other._velocity[0]);
                if(velXComp == 0)
                {
                    int velYComp = Double.compare(_velocity[1], other._velocity[1]);
                    return velYComp;
                }
                else
                {
                    return velXComp;
                }
            }
            else
            {
                return orYComp;
            }
        }
        else
        {
            return orXComp;
        }
    }

    public Vector2D getOrientation()
    {
        if(_orientation == null)
        {
            _orientation = new Vector2D(_rearWheelCP, _frontWheelCP);
        }
        return _orientation;
    }
}
