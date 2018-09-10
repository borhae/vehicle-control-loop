package de.joachim.haensel.phd.scenario.math.geometry.test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.MinimalBlurredSegment;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class MinimalBlurredSegmentTest
{
    @Test
    public void testSimpleMinimalBlurredSegmentThickness()
    {
        List<Position2D> points = new ArrayList<>();
        points.add(new Position2D(0.5, 0.5));
        points.add(new Position2D(1.0, 1.5));
        points.add(new Position2D(1.5, 1.0));
        points.add(new Position2D(2.0, 1.5));
        points.add(new Position2D(2.5, 1.0));
        points.add(new Position2D(3.0, 2.0));
        points.add(new Position2D(3.5, 2.5));
        double expected = 1.1;
        
        MinimalBlurredSegment mbs = new MinimalBlurredSegment(1.5);
        for (Position2D curPoint : points)
        {
            mbs.add(curPoint);
        }
        double actual = mbs.getIsothetickThickness();
        
        assertEquals(expected, actual, Math.ulp(0.0));
    }
}
