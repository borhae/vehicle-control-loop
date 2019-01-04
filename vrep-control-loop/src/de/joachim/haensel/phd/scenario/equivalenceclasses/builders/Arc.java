package de.joachim.haensel.phd.scenario.equivalenceclasses.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.Linspace;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class Arc implements IArcsSegmentContainerElement
{
    public enum CenterAt
    {
        UNDEFINED("undefined"), LEFT("left"), RIGHT("right"), COLLINEAR("collinear");
        
        private String _stringRep;
        
        private CenterAt(String desc)
        {
            _stringRep = desc;
        }

        public static CenterAt create(double side)
        {
            if(side > 0)
            {
                return LEFT;
            }
            else if (side < 0)
            {
                return RIGHT;
            }
            else
            {
                //should never happen actually, but who knows...
                return COLLINEAR;
            }
        }
        
        @Override
        public String toString()
        {
            return _stringRep;
        }
    }

    private static final double LINE_LENGTH = 2.0; //if this is split into lines they should be of this length

    private List<Position2D> _elements;
    private Position2D _center;
    private double _radius;
    private double _iSSE;
    private CenterAt _centerTo;

    public Arc(List<Position2D> elements)
    {
        _elements = elements;
        _center = null; // if creation doesn't work, there is no center that makes sense
        _radius = Double.POSITIVE_INFINITY; // if creation doesn't work the radius is infinitely large
        _iSSE = Double.POSITIVE_INFINITY; // if creation doesn't work the error is infinite
        _centerTo = CenterAt.UNDEFINED;
    }

    @Override
    public String toString()
    {
        return "arc<c_x: " + _center.getX() + ", c_y: " + _center.getY() + ", rad: " + _radius + _elements.get(0) + ", " + _elements.get(_elements.size() - 1) + ">";
    }

    /**
     * Compute the arc from the points that this object was initialized with
     * @param computeISSE should we compute integrated sum of errors 
     */
    public void create(boolean computeISSE)
    {
        //determine middle region
        double oneThird = _elements.size() / 3.0;
        //start of middle region
        int idxBegin = (int)oneThird;
        //end of middle region
        int idxEnd = (int)(_elements.size() - oneThird);

        Position2D first = _elements.get(0);
        Position2D last = _elements.get(_elements.size() - 1);
        Position2D middleElement = null;

        double curLowestError = Double.POSITIVE_INFINITY;

        for(int idx = idxBegin; idx < idxEnd; idx++)
        {
            Position2D cur = _elements.get(idx);
            Position2D center = computeCenter(first, cur, last);
            double averageRadius = 0.0; 
            if(center == null)
            {
                averageRadius = Double.POSITIVE_INFINITY;
            }
            else
            {
                averageRadius = (Position2D.distance(center, first) + Position2D.distance(center, cur) + Position2D.distance(center, last))/3.0;
                double curISE = computeIntegratedSumOfErrorsForArc(center, averageRadius);
                if(curISE < curLowestError)
                {
                    curLowestError = curISE;
                    _center = center;
                    _radius = averageRadius;
                    _iSSE = curLowestError;
                    middleElement = cur;
                }
            }
        }
        if(_center != null)
        {
            double side = Line2D.side(first, middleElement, _center);
            _centerTo = CenterAt.create(side);
        }
    }
    
    /**
     * 
     * @param center
     * @param meanRadius
     * @return
     */
    private double computeIntegratedSumOfErrorsForArc(Position2D center, double radius)
    {
        double isse = 0;
        for (Position2D curPoint : _elements)
        {
            double distancePointCircle = Math.abs(curPoint.distance(center) - radius);
            isse += distancePointCircle * distancePointCircle;
        }
        return isse;
    }
    
    public double getISSE()
    {
        return _iSSE;
    }

    /**
     * Compute circle center for these three points
     * This is a very inefficient implementation (we are constructing a lot of objects, but...)
     * @param first
     * @param middle
     * @param last
     * @return
     */
    private Position2D computeCenter(Position2D first, Position2D middle, Position2D last)
    {
        Vector2D v1 = new Vector2D(first,  middle);
        Vector2D v2 = new Vector2D(middle, last);
        Vector2D per1 = v1.getMiddlePerpendicular();
        Vector2D per2 = v2.getMiddlePerpendicular();
        Position2D circleCenter = Vector2D.unrangedIntersect(per1, per2);
        return circleCenter;
        //maybe replace by something like this:
//        double a1, b1, a2, b2, c1, c2;
//        double xA, yA, xB, yB, xC, yC;
//        xA=p1[0];
//        yA=p1[1];
//        xB=p2[0];
//        yB=p2[1];
//        xC=p3[0];
//        yC=p3[1];
//
//        a1=xA-xB;
//        b1=yA-yB;
//        a2=xA-xC;
//        b2=yA-yC;
//        c1=(xA*xA-xB*xB+yA*yA-yB*yB)/2;
//        c2=(xA*xA-xC*xC+yA*yA-yC*yC)/2;
//        double x,y,dentaY;
//        dentaY=b1*a2-a1*b2;
//        if(dentaY!=0){
//            y= (double)(c1*a2-a1*c2)/(double)dentaY;
//            if (a1!=0) x=(double)(c1-b1*y)/(double)a1;
//            else if(a2!=0) x=(double)(c2-b2*y)/(double)a2;
//            else {
//                cout<<"Error: 3 points of the arc are colinear."<<endl;
//                x=-1;
//                y=-1;
//            }
//        }
//        else
//        {
//            x=-1;
//            y=-1;
//        }
//        return RealPoint(x,y);
    }

    public double getRadius()
    {
        return _radius;
    }

    public Position2D getCenter()
    {
        return _center;
    }

    @Override
    public Position2D getStart()
    {
        return _elements.get(0);
    }

    @Override
    public List<Line2D> getLines()
    {
        List<Line2D> result = new ArrayList<>();
        
        Position2D aNC = _elements.get(0);
        Position2D bNC = _elements.get(_elements.size() - 1);
        Position2D a = Position2D.minus(aNC, _center);
        Position2D b = Position2D.minus(bNC, _center);
        double angle1 = Math.atan2(a.getY(), a.getX());
        double angle2 = Math.atan2(b.getY(), b.getX());

        double angleDist = Math.toDegrees(Math.abs(angle2 - angle1));
        double length = (angleDist/360) * 2.0 * Math.PI * _radius;
        int nrOfLines = (int)(length / LINE_LENGTH);

        List<Double> thetaRange = new ArrayList<>();
        if(_centerTo == CenterAt.LEFT)
        {
            if(angle1 > 0 && angle2 < 0)
            {
                angle2 = 2 * Math.PI + angle2;
            }
            thetaRange = Linspace.linspace(angle1, angle2, nrOfLines);
        }
        else if(_centerTo == CenterAt.RIGHT)
        {
            if(angle1 < 0 && angle2 > 0)
            {
                angle1 = 2 * Math.PI + angle1;
            }
            thetaRange = Linspace.linspace(angle2, angle1, nrOfLines);
        }
        Collections.reverse(thetaRange);
        List<Position2D> points = thetaRange.stream().map(theta -> new Position2D(_center.getX() + Math.cos(theta) * _radius, _center.getY() + Math.sin(theta) * _radius)).collect(Collectors.toList());
        
        Position2D last = null;
        for(int idx = 0; idx < points.size(); idx++)
        {
            Position2D current = points.get(idx);
            if(last != null)
            {
                result.add(new Line2D(last, current));
            }
            last = current;
        }
        return result;
    }

    @Override
    public Position2D getEnd()
    {
        return _elements.get(_elements.size() - 1);
    }

    @Override
    public String toGnuPlotString()
    {
        Position2D a = Position2D.minus(_elements.get(0), _center);
        Position2D b = Position2D.minus(_elements.get(_elements.size() - 1), _center);
        Position2D c = new Position2D(_radius, 0.0); //0 degree
        double angle1 = Math.atan2(a.getY() - c.getY(), a.getX() - c.getX());
        double angle2 = Math.atan2(b.getY() - c.getY(), b.getX() - c.getX());
        
        String result = "" + _center.getX() + " " + _center.getY() + " " + _radius + " " + Math.toDegrees(angle1) + " " + Math.toDegrees(angle2);
        return result;
    }

    private String prefixed(double val)
    {
        String strVal = Double.toString(Math.abs(val));
        if(val >= 0)
        {
            return " + " + strVal;
        }
        else
        {
            return " - " + strVal;
        }
    }

    /**
     * Format:
     * arc center_x, center_y, radius, angle1, angle2, p_0_x, p_0_y, p_n_x, p_n_y, c_x, _c_y
     */
    @Override
    public String toPyPlotString()
    {
        Position2D aNC = _elements.get(0);
        Position2D bNC = _elements.get(_elements.size() - 1);
        Position2D a = Position2D.minus(aNC, _center);
        Position2D b = Position2D.minus(bNC, _center);
        double angle1 = Math.atan2(a.getY(), a.getX());
        double angle2 = Math.atan2(b.getY(), b.getX());
        
        String result = String.format("arc %f %f %f %f %f %f %f %f %f %f %f " + _centerTo.toString(), 
                _center.getX(), _center.getY(), _radius, angle1, angle2, 
                aNC.getX(), aNC.getY(), bNC.getX(), bNC.getY(), _center.getX(), _center.getY());
        return result;
    }
}
