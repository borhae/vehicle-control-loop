package de.joachim.haensel.phd.scenario.equivalenceclasses.builders;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class Arc implements IArcsSegmentContainerElement
{
    private List<Position2D> _elements;
    private Position2D _center;
    private double _radius;
    private double _iSSE;

    public Arc(List<Position2D> elements)
    {
        _elements = elements;
    }

    public void setCenter(Position2D center)
    {
        _center = center;
    }

    public void setRadius(double radius)
    {
        _radius = radius;
    }

    /**
     * Compute the arc from the points that this object was initialized with
     * @param computeISSE should we compute integrated sum of errors (will be done for arcs anyway, just for symmetry to segment)
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

        double curLowestError = Double.MAX_VALUE;

        for(int idx = idxBegin; idx < idxEnd; idx++)
        {
            Position2D cur = _elements.get(idx);
            Position2D center = computeCenter(first, cur, last);
            double averageRadius = (Position2D.distance(center, first) + Position2D.distance(center, cur) + Position2D.distance(center, last))/3.0;
            double curISE = computeIntegratedSumOfErrorsForArc(center, averageRadius);
            if(curISE < curLowestError)
            {
                curLowestError = curISE;
                _center = center;
                _radius = averageRadius;
                _iSSE = curLowestError;
            }
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
    public String toGnuPlotString()
    {
        String result = "" + _center.getX() + " " + _center.getY() + " " + _elements.get(0) + " " + _elements.get(_elements.size() - 1);
        return result;
    }
}
