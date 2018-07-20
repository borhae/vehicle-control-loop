package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.equivalenceclasses.IClassificationResult;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.LineArcsSegmentation;
import de.joachim.haensel.phd.scenario.layerinterface.RandomMapPositionCreator;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;

public class TestLineArcsSegmentation
{
    @Test
    public void testSimplePointBase()
    {
        // following data from file: res/equivalencesegmentationtest/sampleArcLine.tif
        // The figure is taken from the article: "decomposition of a curve into arcs and line segments based on dominant point detection" by
        // Thanh Phuong Nguyen and Isabelle Debled-Rennesson
        
        Deque<Vector2D> dataPoints = new LinkedList<>();
        dataPoints.addLast(new Vector2D(8.0, 7.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(13.0, 43.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(31.0, 88.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(49.0, 115.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(80.0, 146.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(107.0, 164.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(147.0, 178.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(228.0, 178.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(268.0, 164.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(287.0, 222.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(322.0, 263.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(372.0, 290.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(440.0, 290.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(489.0, 263.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(515.0, 236.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(520.0, 227.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(322.0, 7.0, 0.0, 0.0));
        dataPoints.addLast(new Vector2D(592.0, 119.0, 0.0, 0.0));

        LineArcsSegmentation segmenter = new LineArcsSegmentation();
        IClassificationResult segments = segmenter.createSegments(dataPoints);
        //TODO finish this test
    }
    
    @Test
    public void testSegmentTrajectory()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.center(0.0, 0.0);
        Position2D startPosition = RandomMapPositionCreator.createRandomPositonOnStree(roadMap);
        Position2D destinationPosition = RandomMapPositionCreator.createRandomPositonOnStree(roadMap);
        
        IUpperLayerControl upperCtrl = new DefaultNavigationController(1.0, 50);
        Positioner upperLayerSensors = new Positioner(startPosition);
        upperCtrl.initController(upperLayerSensors, roadMap);
        
        upperCtrl.buildSegmentBuffer(destinationPosition, roadMap);
        
        List<Trajectory> allDataPoints = getDataPoints(upperCtrl);
        
        List<List<Trajectory>> slidingWindows = createSlidingWindows(allDataPoints, 10);
        Deque<Deque<Vector2D>> slidingWindowsVectors = transformToVectorDeque(slidingWindows);
        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        Deque<Vector2D> firstWindow = slidingWindowsVectors.getFirst();
        visualizer.addVectorSet(firstWindow, Color.BLACK, 4.0, 0.02);
        visualizer.setVisible(true);
        visualizer.centerContent();
        visualizer.updateVisuals();
        System.out.println("wait");

        LineArcsSegmentation segmenter = new LineArcsSegmentation();
        IClassificationResult result = segmenter.createSegments(slidingWindowsVectors.getFirst());
    }
    
    private Deque<Deque<Vector2D>> transformToVectorDeque(List<List<Trajectory>> slidingWindows)
    {
        Deque<Deque<Vector2D>> result = new LinkedList<>();
        slidingWindows.forEach(curWindow -> result.addLast(new LinkedList<>(curWindow.stream().map(t -> t.getVector()).collect(Collectors.toList()))));
        return result;
    }

    private List<List<Trajectory>> createSlidingWindows(List<Trajectory> allDataPoints, int windowSize)
    {
        List<List<Trajectory>> result = new ArrayList<>();
        for(int cnt = 0; cnt < allDataPoints.size(); cnt++)
        {
            List<Trajectory> newWindow = new ArrayList<>();
            for(int windowCnt = 0; windowCnt < windowSize; windowCnt++)
            {
                int dataPointsIndex = cnt + windowCnt;
                if(dataPointsIndex < allDataPoints.size())
                {
                    newWindow.add(allDataPoints.get(dataPointsIndex));
                }
                else
                {
                    break;
                }
            }
            result.add(newWindow);
        }
        return result;
    }

    private List<Trajectory> getDataPoints(IUpperLayerControl upperCtrl)
    {
        List<Trajectory> result = new ArrayList<>();
        List<Trajectory> intermedidate;;
        intermedidate = upperCtrl.getNewSegments(10);
        while(!intermedidate.isEmpty())
        {
            result.addAll(intermedidate);
            intermedidate = upperCtrl.getNewSegments(10);
        }
        return result;
    }
}
