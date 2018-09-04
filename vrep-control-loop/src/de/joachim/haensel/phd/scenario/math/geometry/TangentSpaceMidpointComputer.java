package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.ArrayList;
import java.util.List;

public class TangentSpaceMidpointComputer
{
    public static List<Midpoint> compute(List<TangentSegment> tangentSpace)
    {
        List<Midpoint> midpoints = new ArrayList<>();
        
        for(int idx = 0; idx < tangentSpace.size() - 1; idx++)
        {
            TangentSegment t1 = tangentSpace.get(idx);
            TangentSegment t2 = tangentSpace.get(idx + 1);
            double x = (t1.getTn2().getX() + t2.getTn1().getX()) / 2.0;
            double y = t1.getTn2().getY();
            Position2D theMidpoint = new Position2D(x, y);
            
            midpoints.add(new Midpoint(theMidpoint, t1, t2));
        }
        return midpoints;
    }
}
