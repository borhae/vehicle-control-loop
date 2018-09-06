package de.joachim.haensel.phd.scenario.math.geometry.test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.geometry.MelkmanHull;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class MelkmanTest
{
    @Test
    public void test5PointInput()
    {
        List<Position2D> input = new ArrayList<>();
        input.add(new Position2D(0.0, 0.0));
        input.add(new Position2D(1.0, 0.0));
        input.add(new Position2D(1.0, 1.0));
        input.add(new Position2D(0.5, 0.5));
        input.add(new Position2D(0.0, 1.0));
        
        List<Position2D> actual = MelkmanHull.hull(input);
        
        List<Position2D> expected = new ArrayList<>();
        expected.add(new Position2D(0.0, 1.0));
        expected.add(new Position2D(0.0, 0.0));
        expected.add(new Position2D(1.0, 0.0));
        expected.add(new Position2D(1.0, 1.0));
        expected.add(new Position2D(0.0, 1.0));
        
        assertEquals(expected, actual);
    }
}
