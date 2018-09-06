package de.joachim.haensel.phd.scenario.math.test;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSegment;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class TangentSpaceTransformerTest
{
    @Test
    public void test1LineTangentSpaceTransformationEquality()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0.0, 0.0, 1.0, 1.0));
        List<TangentSegment> expected = new ArrayList<>();
        expected.add(new TangentSegment(null, new Position2D(0.0, 0.0), new Position2D(0.0, 0.0)));
        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
        assertEquals(expected, actual);
    }

    @Test
    public void test1LineTangentSpaceTransformationInequality()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0.0, 0.0, 1.0, 1.0));
        List<TangentSegment> unexpected = new ArrayList<>();
        unexpected.add(new TangentSegment(null, new Position2D(0.0, 1.0), new Position2D(0.0, 0.0)));
        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
        assertNotEquals(unexpected, actual);
    }

    @Test
    public void test2LineTangentSpaceTransformationEquality()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0.0, 0.0, 1.0, 1.0));
        input.add(new Vector2D(1.0, 1.0, 1.0, -1.0));
        List<TangentSegment> expected = new ArrayList<>();
        expected.add(new TangentSegment(null, new Position2D(0.0, 0.0), new Position2D(0.0, 0.0)));
        expected.add(new TangentSegment(new Position2D(Math.sqrt(2), 0.0), new Position2D(Math.sqrt(2), Math.PI / 2.0), new Position2D(1.0, 1.0)));
        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
        assertThat(actual, is(expected));
    }
    
    @Test
    public void test4LineTangentSpaceTransformationEquality()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0.0, 0.0, 1.0, 1.0));
        input.add(new Vector2D(1.0, 1.0, -1.0, 1.0));
        input.add(new Vector2D(0.0, 2.0, -1.0, -1.0));
        input.add(new Vector2D(-1.0, 1.0, 1.0, -1.0));
        
        List<TangentSegment> expected = new ArrayList<>();
        expected.add(new TangentSegment(null, new Position2D(0.0, 0.0), new Position2D(0.0, 0.0)));
        expected.add(new TangentSegment(new Position2D(Math.sqrt(2), 0.0), new Position2D(Math.sqrt(2), - Math.PI / 2.0), new Position2D(1.0, 1.0)));
        expected.add(new TangentSegment(new Position2D(2 * Math.sqrt(2), - Math.PI / 2.0), new Position2D(2 * Math.sqrt(2), - 2 * Math.PI / 2.0), new Position2D(2.0, 0.0)));
        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
        assertThat(actual, is(expected));
    }
    
    /**
     * TODO finish this test when there is time
     */
    @Test
    public void test12LineTangentSpaceTransformationEquality()
    {
        Position2D ip[] = new Position2D[]{
                new Position2D(0.0, 0.0), //A
                new Position2D(1.0, 1.0), //B
                new Position2D(2.0, 0.0), //C
                new Position2D(5.0, 1.0), //D
                new Position2D(7.0, 2.0), //E
                new Position2D(8.0, 3.0), //F
                new Position2D(8.0, 3.5), //G
                new Position2D(7.5, 3.5), //H
                new Position2D(3.0, 4.0), //I
                new Position2D(1.0, 1.5), //J
                new Position2D(-0.5, 2.5), //K
                new Position2D(-1.0, -1.0), //L
                new Position2D(-0.5, -0.5) //M
        };
        
        Position2D tangentSpacePoints[] = new Position2D[]{
                null,
                new Position2D(0.0, 0.0),
                
                new Position2D(ip[0].distance(ip[1]), 0.0),
                new Position2D(ip[0].distance(ip[1]), Math.PI / 2.0),
                
                new Position2D(ip[0].distance(ip[1]) + ip[1].distance(ip[2]), Math.PI / 2.0),
        };
        
        LinkedList<Vector2D> input = pointArrayToLinkedListOfVectors(ip);
        List<TangentSegment> expected = new ArrayList<>();
        expected.add(new TangentSegment(null, new Position2D(0.0, 0.0), new Position2D(0.0, 0.0)));
        expected.add(new TangentSegment(new Position2D(Math.sqrt(2), 0.0), new Position2D(Math.sqrt(2), Math.PI / 2.0), new Position2D(1.0, 1.0)));
        
        
        
//        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
//        assertThat(actual, is(expected));
    }

    private LinkedList<Vector2D> pointArrayToLinkedListOfVectors(Position2D[] points)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
