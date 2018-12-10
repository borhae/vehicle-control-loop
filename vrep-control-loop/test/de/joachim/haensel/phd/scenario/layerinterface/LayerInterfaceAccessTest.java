package de.joachim.haensel.phd.scenario.layerinterface;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;

public class LayerInterfaceAccessTest
{
    @Test
    public void testSingleRoute()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.center(0.0, 0.0);
        Position2D startPosition = RandomMapPositionCreator.createRandomPositonOnStreet(roadMap);
        Position2D destinationPosition = RandomMapPositionCreator.createRandomPositonOnStreet(roadMap);
        
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
    }
    
    @Test
    public void tesSingleRouteLowerLayerTap()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.center(0.0, 0.0);
        Position2D startPosition = RandomMapPositionCreator.createRandomPositonOnStreet(roadMap);
        Position2D destinationPosition = RandomMapPositionCreator.createRandomPositonOnStreet(roadMap);
        
        IUpperLayerControl upperCtrl = new DefaultNavigationController(1.0, 50);
        Positioner upperLayerSensors = new Positioner(startPosition);
        upperCtrl.initController(upperLayerSensors, roadMap);
        
        upperCtrl.buildSegmentBuffer(destinationPosition, roadMap);
        
        ILowerLayerControl<Object> lowerCtrl = new MockLowerLayerControl();
        
        ITrajectoryRequestListener requestListener = new MockTrajectoryRequestListener();
        ITrajectoryReportListener reportListener = new MockTrajectoryReportListener();
        lowerCtrl.addTrajectoryRequestListener(requestListener);
        lowerCtrl.addTrajectoryReportListener(reportListener);
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
