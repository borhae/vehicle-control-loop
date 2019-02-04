package de.joachim.haensel.phd.scenario.math.geometry.test;



import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

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
        expected.add(new TangentSegment(new Position2D(Math.sqrt(2.0), 0.0), null, new Position2D(1.0, 1.0)));
        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
        assertIterableEquals(expected, actual);
    }

    /**
     * TODO find a way of testing assertNotEquals with lists
     */
//    @Test
//    public void test1LineTangentSpaceTransformationInequality()
//    {
//        LinkedList<Vector2D> input = new LinkedList<>();
//        input.add(new Vector2D(0.0, 0.0, 1.0, 1.0));
//        List<TangentSegment> unexpected = new ArrayList<>();
//        unexpected.add(new TangentSegment(null, new Position2D(0.0, 1.0), new Position2D(0.0, 0.0)));
//        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
//        assertNotEquals(unexpected, actual);
//    }

    @Test
    public void test2LineTangentSpaceTransformationEquality()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0.0, 0.0, 1.0, 1.0));
        input.add(new Vector2D(1.0, 1.0, 1.0, -1.0));
        List<TangentSegment> expected = new ArrayList<>();
        expected.add(new TangentSegment(null, new Position2D(0.0, 0.0), new Position2D(0.0, 0.0)));
        expected.add(new TangentSegment(new Position2D(Math.sqrt(2), 0.0), new Position2D(Math.sqrt(2), Math.PI / 2.0), new Position2D(1.0, 1.0)));
        expected.add(new TangentSegment(new Position2D(2 * Math.sqrt(2), Math.PI / 2.0), null, new Position2D(2.0, 0.0)));
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
        double sqrt2 = Math.sqrt(2);
        expected.add(new TangentSegment(null, new Position2D(0.0, 0.0), new Position2D(0.0, 0.0)));
        expected.add(new TangentSegment(new Position2D(1 * sqrt2, 0.0), new Position2D(1 * sqrt2, - Math.PI / 2.0), new Position2D(1.0, 1.0)));
        expected.add(new TangentSegment(new Position2D(2 * sqrt2, - 1 * Math.PI / 2.0), new Position2D(2 * sqrt2, - 2 * Math.PI / 2.0), new Position2D(0.0, 2.0)));
        expected.add(new TangentSegment(new Position2D(3 * sqrt2, - 2 * Math.PI / 2.0), new Position2D(3 * sqrt2, - 3 * Math.PI / 2.0), new Position2D(-1.0, 1.0)));
        expected.add(new TangentSegment(new Position2D(4 * sqrt2, - 3 * Math.PI / 2.0), null, new Position2D(0.0, 0.0)));
        List<TangentSegment> actual = TangentSpaceTransformer.transform(input);
        assertThat(actual, is(expected));
    }
}
