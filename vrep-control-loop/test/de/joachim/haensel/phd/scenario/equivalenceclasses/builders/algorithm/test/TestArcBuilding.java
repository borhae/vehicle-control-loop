package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Arc;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class TestArcBuilding
{
    @Test
    public void testCreateArc1()
    {
        List<Position2D> input = new ArrayList<>();
        input.add(new Position2D(0.0, 0.0));
        input.add(new Position2D(1.0, 1.0));
        input.add(new Position2D(2.0, 0.0));
        Arc sut = new Arc(input);
        sut.create(true);
        double actualRadius = sut.getRadius();
        Position2D actualCenter = sut.getCenter();
        
        double expectedRadius = 1.0;
        Position2D expectedCenter = new Position2D(1.0, 0.0);
        
        assertEquals(expectedRadius, actualRadius, Math.ulp(0.0));
        assertEquals(expectedCenter, actualCenter);
    }
    
    @Test
    public void testCreateArc2()
    {
        List<Position2D> input = new ArrayList<>();
        input.add(new Position2D(0.0, 0.0));
        input.add(new Position2D(0.5, 0.5));
        input.add(new Position2D(1.0, 1.0));
        input.add(new Position2D(0.4, 0.7));
        input.add(new Position2D(2.0, 0.0));
        Arc sut = new Arc(input);
        sut.create(true);
        double actualRadius = sut.getRadius();
        Position2D actualCenter = sut.getCenter();
        
        double expectedRadius = 1.0;
        Position2D expectedCenter = new Position2D(1.0, 0.0);
        
        assertEquals(expectedRadius, actualRadius, Math.ulp(0.0));
        assertEquals(expectedCenter, actualCenter);
        
    }
}
