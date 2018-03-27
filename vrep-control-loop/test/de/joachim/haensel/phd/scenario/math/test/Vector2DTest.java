package de.joachim.haensel.phd.scenario.math.test;


import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

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
    
    @Test
    public void test2VectorAngle1()
    {
        Vector2D v1 = new Vector2D(0.0, 0.0, 1.0, 0.0);
        Vector2D v2 = new Vector2D(0.0, 0.0, 0.0, 1.0);
        double actual = Vector2D.computeAngle(v1, v2);
        
        double expected = Math.PI/2;
        
        assertEquals(expected, actual, Math.ulp(0.0));
        System.out.println("actual: " + Math.toDegrees(actual));
        System.out.println("actual: " + actual);
    }

    @Test
    public void test2VectorAngle2()
    {
        Vector2D v1 = new Vector2D(0.0, 0.0, 1.0, 0.0);
        Vector2D v2 = new Vector2D(0.0, 0.0, 0.0, -1.0);
        double actual = Vector2D.computeAngle(v1, v2);
        
        double expected = Math.PI/2;
        
        assertEquals(expected, actual, Math.ulp(0.0));
        System.out.println("actual: " + Math.toDegrees(actual));
        System.out.println("actual: " + actual);
    }
    
    @Test
    public void testSideOf()
    {
        Vector2D base = new Vector2D(0.0, 0.0, 1.0, 0.0);
        Vector2D left = new Vector2D(0.0, 0.0, 0.0, 1.0);
        Vector2D right = new Vector2D(0.0, 0.0, 0.0, -1.0);
        
        double actualLeft = base.side(left);
        double actualRight = base.side(right);
        
        double expectedLeft = 1.0;
        double expectedRight = -1.0;
        
        assertEquals(expectedRight, actualRight, Math.ulp(0.0));
        assertEquals(expectedLeft, actualLeft, Math.ulp(0.0));
    }
    
    @Test
    public void testSideOf180DegreeShift()
    {
        Vector2D base = new Vector2D(0.0, 0.0, 1.0, 0.0);
        Vector2D left = new Vector2D(0.0, 0.0, -1.0, 1.0);
        Vector2D right = new Vector2D(0.0, 0.0, -1.0, -1.0);
        
        double actualLeft = base.side(left);
        double actualRight = base.side(right);
        
        double expectedLeft = 1.0;
        double expectedRight = -1.0;
        
        assertEquals(expectedRight, actualRight, Math.ulp(0.0));
        assertEquals(expectedLeft, actualLeft, Math.ulp(0.0));
    }
    
    @Test
    public void testSideOfWithOffset()
    {
        Vector2D base = new Vector2D(0.0, 0.0, 1.0, -1.3);
        Vector2D left = new Vector2D(0.0, 0.0, 1.0, 1.0);
        Vector2D right = new Vector2D(0.0, 0.0, 1.0, -1.0);
        
        double actualLeft1 = base.side(left);
        double actualLeft2 = base.side(right);
        
        double expectedLeft1 = 1.0;
        double expectedLeft2 = 1.0;
        
        assertEquals(expectedLeft2, actualLeft1, Math.ulp(0.0));
        assertEquals(expectedLeft1, actualLeft2, Math.ulp(0.0));
    }

    @Test
    public void testSideOfSmall()
    {
        Vector2D base = new Vector2D(0.0, 0.0, 1.0, 0.0);
        double distance = 0.0000000000000000000000000000001;
        Vector2D left = new Vector2D(0.0, 0.0, 0.0, distance);
        Vector2D right = new Vector2D(0.0, 0.0, 0.0, -distance);
        
        double actualLeft = base.side(left);
        double actualRight = base.side(right);
        
        double expectedLeft = 1.0;
        double expectedRight = -1.0;
        
        assertEquals(expectedRight, actualRight, Math.ulp(0.0));
        assertEquals(expectedLeft, actualLeft, Math.ulp(0.0));
    }
}

