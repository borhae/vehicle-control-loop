package de.joachim.haensel.phd.scenario.math.test;

import org.junit.Test;
import static org.junit.Assert.*;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.sumo2vrep.Position2D;

public class TMatrixTest
{
    @Test
    public void testIdentity()
    {
        Position2D actual = new Position2D(1.0, 1.0);
        TMatrix m = new TMatrix(1.0, 0.0, 0.0);
        actual.transform(m);
        Position2D expected = new Position2D(1.0, 1.0);
        assertTrue(expected.equals(actual, Math.ulp(0.0)));
    }
    
    @Test
    public void testTimes3_5()
    {
        Position2D actual = new Position2D(1.0, 1.0);
        TMatrix m = new TMatrix(3.5, 0.0, 0.0);
        actual.transform(m);
        Position2D expected = new Position2D(3.5, 3.5);
        assertTrue(expected.equals(actual, Math.ulp(0.0)));
    }
    
    @Test
    public void testOffset3_5()
    {
        Position2D actual = new Position2D(1.0, 1.0);
        TMatrix m = new TMatrix(1.0, 3.5, 3.5);
        actual.transform(m);
        Position2D expected = new Position2D(4.5, 4.5);
        assertEquals(expected, actual);
    }

    @Test
    public void testOffset3_5Scale2()
    {
        Position2D actual = new Position2D(1.0, 2.0);
        TMatrix m = new TMatrix(2.0, 3.5, 5);
        actual.transform(m);
        Position2D expected = new Position2D(5.5, 9);
        assertEquals(expected, actual);
    }
}
