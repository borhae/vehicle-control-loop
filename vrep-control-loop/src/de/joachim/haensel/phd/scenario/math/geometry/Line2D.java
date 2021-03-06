package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Line2D
{
    public interface ILineStringFormat
    {
        String format(Line2D line2d);
    }

    private double _x1;
    private double _y1;
    private double _x2;
    private double _y2;
    private double _c;

    public Line2D(String p1, String p2)
    {
        String[] coordinate1 = p1.split(",");
        _x1  = Double.valueOf(coordinate1[0]);
        _y1  = Double.valueOf(coordinate1[1]);
        String[] coordinate2 = p2.split(",");
        _x2  = Double.valueOf(coordinate2[0]);
        _y2  = Double.valueOf(coordinate2[1]);
        _c = Position2D.distance(_x1, _y1, _x2, _y2);
    }
    
    public Line2D(double x1, double y1, double x2, double y2)
    {
        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
        _c = Position2D.distance(_x1, _y1, _x2, _y2);
    }

    public Line2D(Position2D p1, Position2D p2)
    {
        this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public void setLine(Line2D newLine)
    {
        _x1 = newLine._x1;
        _y1 = newLine._y1;
        _x2 = newLine._x2;
        _y2 = newLine._y2;
        _c = Position2D.distance(_x1, _y1, _x2, _y2);
    }

    public double distance(Position2D position)
    {
        return new Vector2D(this).unboundedDistance(position);
    }
    
    /**
     * Perpendicular distance within segment extended by endPointLimit
     * @param pos
     * @param endPointLimit by how much is this line extended and still supposed to result in a value
     * @return
     */
    public double perpendicularDistanceWithEndpointLimit(Position2D pos, double endPointLimit)
    {
        Vector2D v = new Vector2D(this);
        v.lengthen(endPointLimit);
        Position2D intersectionPoint = Vector2D.getPerpendicularIntersection(v, pos);
        if(intersectionPoint != null)
        {
            return Position2D.distance(intersectionPoint, pos);
        }
        else
        {
            return Double.POSITIVE_INFINITY;
        }
    }
    
    public double length()
    {
        return _c;
    }

    public static List<Line2D> createLines(String shape)
    {
        ArrayList<Line2D> result = new ArrayList<Line2D>();
        createLines(Arrays.asList(shape.split(" ")), result);
        return result;
    }

    private static void createLines(List<String> coordinateList, List<Line2D> result)
    {
        if(coordinateList.size() <= 1)
        {
            // a line needs at least two coordinates
            return;
        }
        else
        {
            String lastCoordinate = coordinateList.get(0);
            for(int idx = 1; idx < coordinateList.size(); idx++)
            {
                String curCoordinate = coordinateList.get(idx);
                result.add(new Line2D(lastCoordinate, curCoordinate));
                lastCoordinate = curCoordinate;
            }
        }
    }

    public double getX1()
    {
        return _x1;
    }

    public double getY1()
    {
        return _y1;
    }

    public double getX2()
    {
        return _x2;
    }

    public double getY2()
    {
        return _y2;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("l[");
        String oB = "(";
        String cB = ")";
        String comma = ", ";
        builder.append(oB);
        builder.append(_x1);
        builder.append(comma);
        builder.append(_y1);
        builder.append(cB);
        builder.append(comma);
        builder.append(oB);
        builder.append(_x2);
        builder.append(comma);
        builder.append(_y2);
        builder.append(cB);
        builder.append("]");
        return builder.toString();
    }

    public static LinkedList<Vector2D> lineListToVectorList(List<Line2D> route)
    {
        return route.stream().map(line -> new Vector2D(line)).collect(Collectors.toCollection(LinkedList::new));
    }

    public Position2D getP1()
    {
        return new Position2D(_x1, _y1);
    }

    public Position2D getP2()
    {
        return new Position2D(_x2, _y2);
    }

    public void setP1(Position2D position)
    {
        _x1 = position.getX();
        _y1 = position.getY();
        _c = Position2D.distance(_x1, _y1, _x2, _y2);
    }

    public void setP2(Position2D position)
    {
        _x2 = position.getX();
        _y2 = position.getY();
        _c = Position2D.distance(_x1, _y1, _x2, _y2);
    }

    /**
     * Computes on which side the the point is in relation to a line a to b
     * Left and right means that you're looking from a into the direction of b  
     * and then this result will tell you whether you'll find m to your right or left side

     * @param a first point of line
     * @param b second point of line
     * @param m point where we want to know where it is with respect to the line
     * @return side decoded as: -1 to the right, 0 collinear, 1 to the left
     */
    public static double side(Position2D a, Position2D b, Position2D m)
    {
        double determinant = (b.getX() - a.getX()) * (m.getY() - a.getY()) - (b.getY() - a.getY()) * (m.getX() - a.getX());
        return Math.signum(determinant);
    }


    /**
     * Computes on which side point m is in relation to this line.
     * Left and right means that you're looking from a into the direction of b  
     * and then this result will tell you whether you'll find m to your right or left side

     * @param a first point of line
     * @param b second point of line
     * @param m point where we want to know where it is with respect to the line
     * @return side decoded as: -1 to the right, 0 collinear, 1 to the left
     */
    public double side(Position2D m)
    {
        double determinant = (_x2 - _x1) * (m.getY() - _y1) - (_y2 - _y1) * (m.getX() - _x1);
        return Math.signum(determinant);
    }

    /**
     * rather use this
     * @param pos
     * @param epsilon
     * @return
     */
    public boolean contains(Position2D pos, double epsilon)
    {
        Vector2D v = new Vector2D(this);
        Position2D perpendicularIntersection = Vector2D.getPerpendicularIntersection(v, pos);
        if(perpendicularIntersection == null)
        {
            return false;
        }
        else
        {
            double distance = perpendicularIntersection.distance(pos);
            if(distance > epsilon)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    public String toPyplotString()
    {
        return String.format("seg %.6f %.6f %.6f %.6f", _x1, _y1, _x2, _y2);
    }

    public String toPyplotString(String color)
    {
        return toPyplotString() + " " + color;
    }

    public double distancePerpendicularOrEndpoints(Position2D position)
    {
        double result = Double.POSITIVE_INFINITY;
        Vector2D v = new Vector2D(this);
        Position2D intersection = Vector2D.getPerpendicularIntersection(v, position);
        if(intersection != null)
        {
            result = intersection.distance(position);
        }
        else
        {
            result = Math.min(position.distance(getP1()), position.distance(getP2()));
        }
        return result;
    }

    public Position2D perpendicularIntersection(Position2D p)
    {
        return Vector2D.getPerpendicularIntersection(new Vector2D(this), p);
    }

    public String toStringFormatted(ILineStringFormat formatter)
    {
        return formatter.format(this);
    }
}
