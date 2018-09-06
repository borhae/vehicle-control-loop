package de.joachim.haensel.phd.scenario.math.test;

import static org.junit.Assert.*;
import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class Line2DTest
{
    @Test
    public void testSideOflineLeft()
    {
        Line2D line = new Line2D(0.0, 0.0, 1.0, 0.0);
        Position2D point = new Position2D(0.5, 1.0);
        
        double actual = Line2D.side(line.getP1(), line.getP2(), point);
        double expected = 1.0;
        
        assertEquals(expected, actual, Math.ulp(0.0));
    }
    
    
    @Test
    public void testSideOflineRight()
    {
        Line2D line = new Line2D(0.0, 0.0, 1.0, 0.0);
        Position2D point = new Position2D(0.5, -1.0);
        
        double actual = Line2D.side(line.getP1(), line.getP2(), point);
        double expected = -1.0;
        
        assertEquals(expected, actual, Math.ulp(0.0));
    }
    
    @Test
    public void testSideOflineCollinear()
    {
        Line2D line = new Line2D(0.0, 0.0, 1.0, 0.0);
        Position2D point = new Position2D(0.5, 0.0);
        
        double actual = Line2D.side(line.getP1(), line.getP2(), point);
        double expected = 0.0;
        
        assertEquals(expected, actual, Math.ulp(0.0));
    }
    
    @Test
    public void testSideOflineLeft1()
    {
        Line2D line = new Line2D(0.0, 0.0, -1.0, -1.0);
        Position2D point = new Position2D(0.5, 0.0);
        
        double actual = Line2D.side(line.getP1(), line.getP2(), point);
        double expected = 1.0;
        
        assertEquals(expected, actual, Math.ulp(0.0));
    }
    
    
    @Test
    public void testSideOflineRight1()
    {
        Line2D line = new Line2D(0.0, 0.0, -1.0, -1.0);
        Position2D point = new Position2D(-0.5, 0.0);
        
        double actual = Line2D.side(line.getP1(), line.getP2(), point);
        double expected = -1.0;
        
        assertEquals(expected, actual, Math.ulp(0.0));
    }
    
    @Test
    public void testSideOflineCollinear1()
    {
        Line2D line = new Line2D(0.0, 0.0, -1.0, -1.0);
        Position2D point = new Position2D(0.5, 0.5);
        
        double actual = Line2D.side(line.getP1(), line.getP2(), point);
        double expected = 0.0;
        
        assertEquals(expected, actual, Math.ulp(0.0));
    }
}
