package de.joachim.haensel.phd.scenario.math.test;

import java.util.List;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class Vector2DTest
{
    @Test
    public void testCircleIntersectionAtX0Point5Y0()
    {
        Vector2D vector = new Vector2D(0, 0, 1, 0);
        Position2D circleCenter = new Position2D(0, 0);
        double radius = 0.5;
        
        List<Vector2D> actuals = Vector2D.circleIntersection(vector, circleCenter, radius);
        Vector2D expected = new Vector2D(0, 0, 0.5, 0);

        assert(actuals.size() == 1);
        assert(expected.equals(actuals.get(0)));
    }
    
    @Test
    public void testCircleIntersectionAtX0Y1()
    {
        Position2D base = new Position2D(-2, 3);
        Position2D tip = new Position2D(0.5, 0.5);
        Vector2D vector = new Vector2D(base, tip);
        Position2D circleCenter = new Position2D(0.5, 0.5);
        double radius = Math.sqrt(0.5);
        
        List<Vector2D> actuals = Vector2D.circleIntersection(vector, circleCenter, radius);
        Vector2D expected = new Vector2D(0, 0, 0, 1);
        
        assert(actuals.size() == 1);
        Position2D actualTip = actuals.get(0).getTip();
        Vector2D actual = new Vector2D(new Position2D(0, 0), actualTip);
        assert(expected.equals(actual));
    }
}

