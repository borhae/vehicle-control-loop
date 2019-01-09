package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.ArcSegmentDecompositionAlgorithmByNgoEtAl;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSegment;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;

@RunWith(Parameterized.class)
public class SingleWindowParameterizedDecompositionTest
{
    private double _thickness;
    private double _alphaMax;
    private double _nbCirclePoint;
    private double _isseTol; // if bigger than 1 it favours arcs, smaller than 1 segments
    private double _maxRadius;
    private String _suffix;
    
    @Parameterized.Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]
        {
//            {1.0, Math.PI / 4.0, 3, 4.0, "1_"},
//            {0.5, Math.PI / 4.0, 3, 4.0, "2_"},
//            {0.4, Math.PI / 4.0, 3, 4.0, "3_"},
//            {0.2, Math.PI / 4.0, 3, 4.0, "4_"},
//            {0.1, Math.PI / 4.0, 3, 4.0, "5_"},
//            {0.4, Math.PI / 4.0, 3, 2.0, "6_"},
//            {0.2, Math.PI / 4.0, 3, 2.0, "7_"},
//            {0.8, Math.PI / 4.0, 3, 2.0, "8_"},
//            {0.3, Math.PI / 4.0, 3, 2.0, "9_"},
//            {0.3, Math.PI / 4.0, 3, 1.5, "10_"},
//            {0.3, Math.PI / 4.0, 3, 0.5, "11_"},
//            {0.3, Math.PI / 4.0, 3, 0.05, "12_"},
            {0.3, Math.PI / 4.0, 3, 1.0, Double.POSITIVE_INFINITY, "13_"}, // -> best before introduction of maxRadius: don't prefer arcs over segments or the other way round, small 
//            {0.2, Math.PI / 4.0, 3, 1.2, "14_"},
//            {0.2, Math.PI, 3, 1.2, "15_"},
//            {0.2, Math.PI / 2.0, 3, 1.2, "16_"},
//            {0.2, Math.PI / 4.0, 3, 1.2, "17_"},
//            {0.2, Math.PI / 8.0, 3, 1.2, "18_"}
            {0.3, Math.PI / 4.0, 3, 1.0, 100000, "19_"} // -> work with maxRadius: don't prefer arcs over segments or the other way round, small 
        });
    }

    public SingleWindowParameterizedDecompositionTest(double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius, String suffix)
    {
        _thickness = thickness;
        _alphaMax = alphaMax;
        _nbCirclePoint = nbCirclePoint;
        _isseTol = isseTol;
        _maxRadius = maxRadius;
        _suffix = suffix;
    }
    
    @Test
    public void testMultipleDecompositinosTrajectoryBeginningOfRoute()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerTransformMatrix = roadMap.center(0.0, 0.0);

        //uncentered positions measured by sumo net-edit tool
        Position2D startPosition = new Position2D(5531.34,5485.96);
        Position2D destinationPosition = new Position2D(5879.87,4886.08);
        
        startPosition.transform(centerTransformMatrix);
        destinationPosition.transform(centerTransformMatrix);
        startPosition = roadMap.getClosestPointOnMap(startPosition);
        destinationPosition = roadMap.getClosestPointOnMap(destinationPosition);
        
        IUpperLayerControl upperCtrl = new DefaultNavigationController(1.0, 50);
        Positioner upperLayerSensors = new Positioner(startPosition);
        upperCtrl.initController(upperLayerSensors, roadMap);
        
        upperCtrl.buildSegmentBuffer(destinationPosition, roadMap);
        
        List<TrajectoryElement> allDataPoints = getDataPoints(upperCtrl);
        
        int windowSize = 30;
        List<List<TrajectoryElement>> slidingWindows = createSlidingWindows(allDataPoints, windowSize, allDataPoints.size() / windowSize);
        slidingWindows = slidingWindows.subList(396, 397);
        String basePath = "./res/equivalencesegmentationtest/segmentationtuning/";
        Consumer<? super IndexAdder<List<TrajectoryElement>>> decompose = 
                curWindow -> decomposeWindow(curWindow.v(), curWindow.idx(), _thickness, _alphaMax, _nbCirclePoint, _isseTol, _maxRadius,
                        basePath + "sampleWholeRoute" + _suffix, basePath + "sampleWholeRouteTangentSpace" + _suffix, basePath + "sampleWholeRouteSegmentation" + _suffix);
        slidingWindows.stream().map(IndexAdder.indexed()).forEach(decompose);
    }
    
    @Test
    public void testTuneMultipleDecompositionsTrajectoryEndOfRoute2()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerTransformMatrix = roadMap.center(0.0, 0.0);

        //uncentered positions measured by sumo net-edit tool
        Position2D startPosition = new Position2D(6045.44, 2991.89);
        Position2D destinationPosition = new Position2D(5867.1, 4934.41);
        
        startPosition.transform(centerTransformMatrix);
        destinationPosition.transform(centerTransformMatrix);
        startPosition = roadMap.getClosestPointOnMap(startPosition);
        destinationPosition = roadMap.getClosestPointOnMap(destinationPosition);
        
        IUpperLayerControl upperCtrl = new DefaultNavigationController(1.0, 50);
        Positioner upperLayerSensors = new Positioner(startPosition);
        upperCtrl.initController(upperLayerSensors, roadMap);
        
        upperCtrl.buildSegmentBuffer(destinationPosition, roadMap);
        
        List<TrajectoryElement> allDataPoints = getDataPoints(upperCtrl);
        
        int windowSize = 30;
        List<List<TrajectoryElement>> slidingWindows = createSlidingWindows(allDataPoints, windowSize, allDataPoints.size() / windowSize);
//        slidingWindows = slidingWindows.subList(1400, 1500);
        String basePath = "./res/equivalencesegmentationtest/segmentationtuning/";
        Consumer<? super IndexAdder<List<TrajectoryElement>>> decompose = 
                curWindow -> decomposeWindow(curWindow.v(), curWindow.idx(), _thickness, _alphaMax, _nbCirclePoint, _isseTol, _maxRadius,
                        basePath + "sampleWholeRoute" + _suffix, basePath + "sampleWholeRouteTangentSpace" + _suffix, basePath + "sampleWholeRouteSegmentation" + _suffix);
        slidingWindows.stream().map(IndexAdder.indexed()).forEach(decompose);
    }
    
    private List<IArcsSegmentContainerElement> decomposeWindow(List<TrajectoryElement> curWindow, int curIdx, 
            double thickness, double alphaMax, double nbCirclePoint, double isseTol, 
            double maxRadius, String routeFileName, String tangentSpaceFileName, String decompositionFileName)
    {
        String counter = String.format("%06d", curIdx);
        Deque<Vector2D> routeVectors = curWindow.stream().map(trajectory -> trajectory.getVector()).collect(Collectors.toCollection(new Supplier<Deque<Vector2D>>() {
            @Override
            public Deque<Vector2D> get()
            {
                return new LinkedList<>();
            }
        }));
        List<String> routeAsString = routeVectors.stream().map(vec -> vec.getBase().toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File(routeFileName + counter + ".pyplot").toPath(), routeAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        
        List<TangentSegment> tangentSpaceRoute = TangentSpaceTransformer.transform(routeVectors);
        List<String> tangentSpaceAsString = TangentSpaceTransformer.tangentSpaceAsMatplotFile(tangentSpaceRoute);
        
        try
        {
            Files.write(new File(tangentSpaceFileName + counter + ".pyplot").toPath(), tangentSpaceAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        
        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(routeVectors, thickness, alphaMax, nbCirclePoint, isseTol, maxRadius);
        List<String> elementsAsString = segments.stream().map(element -> element.toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File(decompositionFileName + counter + ".pyplot").toPath(), elementsAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        return segments;
    }

    private List<List<TrajectoryElement>> createSlidingWindows(List<TrajectoryElement> allDataPoints, int windowSize, int amount)
    {
        List<List<TrajectoryElement>> result = new ArrayList<>();
        for(int cnt = 0; cnt < allDataPoints.size(); cnt++)
        {
            List<TrajectoryElement> newWindow = new ArrayList<>();
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
    
    private List<TrajectoryElement> getDataPoints(IUpperLayerControl upperCtrl)
    {
        List<TrajectoryElement> result = new ArrayList<>();
        List<TrajectoryElement> intermedidate;;
        intermedidate = upperCtrl.getNewSegments(10);
        while(!intermedidate.isEmpty())
        {
            result.addAll(intermedidate);
            intermedidate = upperCtrl.getNewSegments(10);
        }
        return result;
    }
}
