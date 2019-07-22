package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.test;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.segmenting.Arc;

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
    
    @Test
    public void testCreateArcAndComputLine()
    {
        List<Position2D> input = new ArrayList<>();
        input.add(new Position2D(0.0, 0.0));
        input.add(new Position2D(0.5, 0.5));
        input.add(new Position2D(1.0, 1.0));
        input.add(new Position2D(0.4, 0.7));
        input.add(new Position2D(2.0, 0.0));
        Arc sut = new Arc(input);
        sut.create(true);
        List<Line2D> lines = sut.getLines();
        List<String> actual = lines.stream().map(line -> line.toPyplotString()).collect(Collectors.toList());
        try
        {
            Files.write(new File("./res/arctest/arc_expected.pyplot").toPath(), Arrays.asList(new String[]{sut.toPyPlotString()}), Charset.defaultCharset());
            Files.write(new File("./res/arctest/arc_actual.pyplot").toPath(), actual, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
