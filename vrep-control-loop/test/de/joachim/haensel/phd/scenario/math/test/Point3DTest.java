package de.joachim.haensel.phd.scenario.math.test;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import de.joachim.haensel.phd.scenario.math.geometry.Point3D;

public class Point3DTest
{
    @Test
    public void testCrossProduct1m82and2m71()
    {
        Point3D a = new Point3D(1, -8, 2);
        Point3D b = new Point3D(2, -7, 1);
        
        Point3D expected = new Point3D(6, 3, 9);
        Point3D actual = Point3D.crossProduct(a, b);
        
        assertEquals(expected, actual);
    }
}
