package de.joachim.haensel.phd.scenario.navigation.visualization;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;

public class Vector2DVisualizerTest
{
    @Test
    public void testVisual3Vectors()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        List<Vector2D> input = new ArrayList<>();
        input.add(new Vector2D(0, 0, 10, 10));
        input.add(new Vector2D(10, 10, 0, 20));
        input.add(new Vector2D(10, 30, 20, 40));
        visualizer.setVectors(input);
        visualizer.updateVisuals();
        visualizer.setVisible(true);
        System.out.println("stop");
    }
}
