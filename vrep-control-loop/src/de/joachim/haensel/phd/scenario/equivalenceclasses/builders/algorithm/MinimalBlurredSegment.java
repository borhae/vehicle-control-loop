package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.MelkmanHull;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class MinimalBlurredSegment
{
    private double _thickness;
    
    private double _a;
    private double _b;
    private double _o;
    private double _m;

    private List<Position2D> _elements;
    private MelkmanHull _melkmanHull;

    public MinimalBlurredSegment(double thickness)
    {
        _thickness = thickness;
        _elements = new ArrayList<Position2D>();
        _melkmanHull = new MelkmanHull();
        //describes a MBS with _m <= ax - by <= _m + _o
        _a = 0.0;
        _b = 0.0;
        _o = 0.0;
        _m = 0.0;
    }

    public void add(Position2D newPoint)
    {
        // TODO Auto-generated method stub. add new point to minimal blurred segment, recompute a, b, omega and my. 
        _elements.add(newPoint);
        _melkmanHull.add(newPoint);
    }

    public void clear()
    {
        _elements.clear();
        _a = 0.0;
        _b = 0.0;
        _o = 0.0;
        _m = 0.0;
    }

    public boolean staysMinimalBlurredSegmentWith(Position2D asPosition2D)
    {
        // TODO Auto-generated method stub. Test whether the addition of the point will violate the thickness property of this blurred segment. Check whether _thickness is still ok with new omega
        
        return false;
    }

}
