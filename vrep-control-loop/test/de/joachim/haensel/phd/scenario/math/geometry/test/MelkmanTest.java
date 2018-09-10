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
    public void test3PointInput()
    {
        List<Position2D> input = new ArrayList<>();
        input.add(new Position2D(0.0, 0.0));
        input.add(new Position2D(1.0, 0.0));
        input.add(new Position2D(1.0, 1.0));
        
        List<Position2D> actual = MelkmanHull.hull(input);
        
        List<Position2D> expected = new ArrayList<>();
        expected.add(new Position2D(1.0, 1.0));
        expected.add(new Position2D(0.0, 0.0));
        expected.add(new Position2D(1.0, 0.0));
        expected.add(new Position2D(1.0, 1.0));
        
        assertEquals(expected, actual);
    }

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
    
    @Test
    public void test6PointInput()
    {
        List<Position2D> input = new ArrayList<>();
        input.add(new Position2D(0.0, 0.0));
        input.add(new Position2D(1.0, 0.0));
        input.add(new Position2D(1.0, 1.0));
        input.add(new Position2D(0.5, 0.5));
        input.add(new Position2D(0.0, 1.0));
        input.add(new Position2D(2.0, 2.0));
        
        List<Position2D> actual = MelkmanHull.hull(input);
        
        List<Position2D> expected = new ArrayList<>();
        expected.add(new Position2D(2.0, 2.0));
        expected.add(new Position2D(0.0, 1.0));
        expected.add(new Position2D(0.0, 0.0));
        expected.add(new Position2D(1.0, 0.0));
        expected.add(new Position2D(2.0, 2.0));
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void test5PointIncrementalInput()
    {
        List<Position2D> expected1 = new ArrayList<>();
        expected1.add(new Position2D(0.0, 0.0));
        expected1.add(new Position2D(0.0, 0.0));

        List<Position2D> expected2 = new ArrayList<>();
        expected2.add(new Position2D(1.0, 0.0));
        expected2.add(new Position2D(0.0, 0.0));
        expected2.add(new Position2D(1.0, 0.0));

        List<Position2D> expected3 = new ArrayList<>();
        expected3.add(new Position2D(1.0, 1.0));
        expected3.add(new Position2D(0.0, 0.0));
        expected3.add(new Position2D(1.0, 0.0));
        expected3.add(new Position2D(1.0, 1.0));

        //between 3rd and 5th point we add a point in the middle so the hull shouldn't change
        List<Position2D> expected4 = expected3;
        
        List<Position2D> expected5 = new ArrayList<>();
        expected5.add(new Position2D(0.0, 1.0));
        expected5.add(new Position2D(0.0, 0.0));
        expected5.add(new Position2D(1.0, 0.0));
        expected5.add(new Position2D(1.0, 1.0));
        expected5.add(new Position2D(0.0, 1.0));

        MelkmanHull hullComputer = new MelkmanHull();
        hullComputer.add(new Position2D(0.0, 0.0));
        List<Position2D> actual1 = hullComputer.getHull();
        assertEquals(expected1, actual1);

        hullComputer.add(new Position2D(1.0, 0.0));
        List<Position2D> actual2 = hullComputer.getHull();
        assertEquals(expected2, actual2);
        
        hullComputer.add(new Position2D(1.0, 1.0));
        List<Position2D> actual3 = hullComputer.getHull();
        assertEquals(expected3, actual3);
        
        hullComputer.add(new Position2D(0.5, 0.5));
        List<Position2D> actual4 = hullComputer.getHull();
        assertEquals(expected4, actual4);

        hullComputer.add(new Position2D(0.0, 1.0));
        List<Position2D> actual5 = hullComputer.getHull();
        assertEquals(expected5, actual5);
    }
    
    @Test
    public void test6PointIncrementalInput()
    {
        List<Position2D> expected1 = new ArrayList<>();
        expected1.add(new Position2D(0.0, 0.0));
        expected1.add(new Position2D(0.0, 0.0));

        List<Position2D> expected2 = new ArrayList<>();
        expected2.add(new Position2D(1.0, 0.0));
        expected2.add(new Position2D(0.0, 0.0));
        expected2.add(new Position2D(1.0, 0.0));

        List<Position2D> expected3 = new ArrayList<>();
        expected3.add(new Position2D(1.0, 1.0));
        expected3.add(new Position2D(0.0, 0.0));
        expected3.add(new Position2D(1.0, 0.0));
        expected3.add(new Position2D(1.0, 1.0));

        //between 3rd and 5th point we add a point in the middle so the hull shouldn't change
        List<Position2D> expected4 = expected3;
        
        List<Position2D> expected5 = new ArrayList<>();
        expected5.add(new Position2D(0.0, 1.0));
        expected5.add(new Position2D(0.0, 0.0));
        expected5.add(new Position2D(1.0, 0.0));
        expected5.add(new Position2D(1.0, 1.0));
        expected5.add(new Position2D(0.0, 1.0));

        //should remove (0.0, 0.0) which was present in all the other cases
        List<Position2D> expected6 = new ArrayList<>();
        expected6.add(new Position2D(-1.0, -1.0));
        expected6.add(new Position2D(1.0, 0.0));
        expected6.add(new Position2D(1.0, 1.0));
        expected6.add(new Position2D(0.0, 1.0));
        expected6.add(new Position2D(-1.0, -1.0));

        MelkmanHull hullComputer = new MelkmanHull();
        hullComputer.add(new Position2D(0.0, 0.0));
        List<Position2D> actual1 = hullComputer.getHull();
        assertEquals(expected1, actual1);

        hullComputer.add(new Position2D(1.0, 0.0));
        List<Position2D> actual2 = hullComputer.getHull();
        assertEquals(expected2, actual2);
        
        hullComputer.add(new Position2D(1.0, 1.0));
        List<Position2D> actual3 = hullComputer.getHull();
        assertEquals(expected3, actual3);
        
        hullComputer.add(new Position2D(0.5, 0.5));
        List<Position2D> actual4 = hullComputer.getHull();
        assertEquals(expected4, actual4);

        hullComputer.add(new Position2D(0.0, 1.0));
        List<Position2D> actual5 = hullComputer.getHull();
        assertEquals(expected5, actual5);

        hullComputer.add(new Position2D(-1.0, -1.0));
        List<Position2D> actual6 = hullComputer.getHull();
        assertEquals(expected6, actual6);
    }
}
