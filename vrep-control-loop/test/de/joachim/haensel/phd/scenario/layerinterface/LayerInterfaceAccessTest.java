package de.joachim.haensel.phd.scenario.layerinterface;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.vehicle.DefaultNavigationController;
import de.joachim.haensel.vehicle.IUpperLayerControl;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class LayerInterfaceAccessTest
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeClass
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }
    
    @AfterClass
    public static void tearDownVrep() 
    {
        _vrep.simxFinish(_clientID);
    }
    
    @After
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }

    @Test
    public void testSingleRoute()
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
        visualizer.addVectorSet(firstWindow, Color.BLACK, 0.1, 0.1);
        visualizer.setVisible(true);
        System.out.println("wait");
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
