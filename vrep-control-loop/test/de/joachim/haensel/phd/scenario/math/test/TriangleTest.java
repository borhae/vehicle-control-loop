package de.joachim.haensel.phd.scenario.math.test;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.scenario.math.Triangle;
import de.joachim.haensel.phd.scenario.math.TriangleError;
import de.joachim.haensel.phd.scenario.math.TriangleSolver;

public class TriangleTest
{
    @Test
    public void testGetB()
    {
        Triangle t = new Triangle();
        t.setA(5);
        t.setC(3);
        t.setAlpha(Math.PI/2.0f);
        float expected = 3.0f;
        float actual = t.getB();
        assertEquals(expected, actual, 0.00000000000000001f);
    }
    
    @Test
    public void testGetBNetAlgorithm()
    {
        TriangleSolver t = new TriangleSolver();
        t.seta(5);
        t.setc(3);
        t.setAlpha(90);
        try
        {
            t.solveTriangle();
        }
        catch (TriangleError exc)
        {
            exc.printStackTrace();
        }
        float expected = 4.0f;
        float actual = (float)t.getb();
        assertEquals(expected, actual, 0.00000000000000001f);
    }

}
