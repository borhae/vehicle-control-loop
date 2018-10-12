package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Arc;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Segment;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

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
        VectorContentElement visualizee = new VectorContentElement(input, Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(visualizee);
        visualizer.setVisible(true);
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
    
    @Test
    public void testVisualize4VectorsCentered()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        Deque<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 10));
        VectorContentElement visualizee = new VectorContentElement(input, Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(visualizee);
        visualizee.addVector(new Vector2D(10, 10, 0, 20));
        visualizee.addVector(new Vector2D(10, 30, 20, 40));
        visualizer.updateVisuals();
        visualizer.centerContent();
        visualizer.setVisible(true);
        System.out.println("wait");
    }
    
    @Test
    public void testVisualize1Line()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        List<IArcsSegmentContainerElement> input = new ArrayList<IArcsSegmentContainerElement>();
        input.add(new Segment(new Line2D(10.0, 10.0, 100.0, 100.0)));
        ArcSegmentContentElement lines = new ArcSegmentContentElement(input , Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(lines);
        visualizer.updateVisuals();
        visualizer.centerContent();
        visualizer.showOnScreen(2);        
        visualizer.setVisible(true);
        System.out.println("wait");
    }
    
    @Test
    public void testVisualizeSimpleArc()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        List<IArcsSegmentContainerElement> input = new ArrayList<>();
        List<Position2D> startEnd = Arrays.asList(new Position2D[]{new Position2D(0.0, 0.0), new Position2D(100.0, 0.0)});
        Arc arc = new Arc(startEnd);
        arc.setCenter(new Position2D(50.0, 0.0));
        arc.setRadius(50.0);
        ArcSegmentContentElement visualizee = new ArcSegmentContentElement(input , Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(visualizee);
//        visualizer.centerContent();
        visualizer.setVisible(true);
        visualizer.updateVisuals();
        System.out.println("wait");
    }
    
    @Test
    public void testVisualize1Arc()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        List<IArcsSegmentContainerElement> input = new ArrayList<IArcsSegmentContainerElement>();
        ArrayList<Position2D> arcPoints = new ArrayList<Position2D>();
        arcPoints.add(new Position2D(10.0, 10.0));
        arcPoints.add(new Position2D(300.0, 300));
        arcPoints.add(new Position2D(310.0, 10.0));
        Arc arc = new Arc(arcPoints);
        arc.create(false);
        input.add(arc);
        ArcSegmentContentElement lines = new ArcSegmentContentElement(input , Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(lines);
        visualizer.updateVisuals();
        visualizer.centerContent();
        visualizer.showOnScreen(2);        
        visualizer.setVisible(true);
        System.out.println("wait");
    }
    
    @Test
    public void testVisualize1Arc1Line()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        List<IArcsSegmentContainerElement> input = new ArrayList<IArcsSegmentContainerElement>();
        ArrayList<Position2D> arcPoints = new ArrayList<Position2D>();
        Position2D a = new Position2D(0.0, 0.0);
        Position2D b = new Position2D(100.0, 120);
        Position2D c = new Position2D(200.0, 200.0);
        Position2D d = new Position2D(300.0, 120);
        Position2D e = new Position2D(400.0, 0.0);
        arcPoints.add(a);
        arcPoints.add(b);
        arcPoints.add(c);
        arcPoints.add(d);
        arcPoints.add(e);
        Arc arc = new Arc(arcPoints);
        arc.create(false);
        input.add(arc);
        Segment seg = new Segment(new Line2D(400.0, 400.0, 800.0, 410.0));
        input.add(seg);
        Segment seg2 = new Segment(new Line2D(a, b));
        input.add(seg2);
        Segment seg3 = new Segment(new Line2D(b, c));
        input.add(seg3);
        Segment seg4 = new Segment(new Line2D(c, d));
        input.add(seg4);
        Segment seg5 = new Segment(new Line2D(d, e));
        input.add(seg5);
        ArcSegmentContentElement elements = new ArcSegmentContentElement(input , Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(elements);
        visualizer.updateVisuals();
        visualizer.centerContent();
        visualizer.showOnScreen(2);        
        visualizer.setVisible(true);
        System.out.println("wait");
    }
    
    @Test
    public void testVisualize1Arc1LineShorter()
    {
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        List<IArcsSegmentContainerElement> input = new ArrayList<IArcsSegmentContainerElement>();
        ArrayList<Position2D> arcPoints = new ArrayList<Position2D>();
        Position2D a = new Position2D(0.0, 0.0);
        Position2D b = new Position2D(100.0, 120);
        Position2D c = new Position2D(200.0, 200.0);
        Position2D d = new Position2D(300.0, 120);
        arcPoints.add(a);
        arcPoints.add(b);
        arcPoints.add(c);
        arcPoints.add(d);
        Arc arc = new Arc(arcPoints);
        arc.create(false);
        input.add(arc);
        Segment seg = new Segment(new Line2D(400.0, 400.0, 800.0, 410.0));
        input.add(seg);
        Segment seg2 = new Segment(new Line2D(a, b));
        input.add(seg2);
        Segment seg3 = new Segment(new Line2D(b, c));
        input.add(seg3);
        Segment seg4 = new Segment(new Line2D(c, d));
        input.add(seg4);
        ArcSegmentContentElement elements = new ArcSegmentContentElement(input , Color.BLUE, new BasicStroke(1.0f));
        visualizer.addContentElement(elements);
        visualizer.updateVisuals();
        visualizer.centerContent();
        visualizer.showOnScreen(2);        
        visualizer.setVisible(true);
        System.out.println("wait");
    }
}
