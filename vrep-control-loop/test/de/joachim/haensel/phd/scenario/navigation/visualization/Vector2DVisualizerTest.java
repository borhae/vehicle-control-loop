package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;

public class Vector2DVisualizerTest
{
    @Test
    public void testVisual3Vectors()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        Deque<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 10));
        input.add(new Vector2D(10, 10, 0, 20));
        input.add(new Vector2D(10, 30, 20, 40));
        visualizer.addVectorSet(input, Color.BLUE);
        visualizer.updateVisuals();
        visualizer.setVisible(true);
        System.out.println("stop");
    }
    
    @Test
    public void testVisualize4VectorsSequentially()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        Deque<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 10));
        ContentElememnt visualizee = new ContentElememnt(input, Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(visualizee);
        visualizer.updateVisuals();
        visualizer.setVisible(true);
        System.out.println("one");
        visualizee.addVector(new Vector2D(10, 10, 0, 20));
        visualizer.updateVisuals();
        System.out.println("two");
        visualizee.addVector(new Vector2D(10, 30, 20, 40));
        visualizer.updateVisuals();
        System.out.println("three");
    }
}
