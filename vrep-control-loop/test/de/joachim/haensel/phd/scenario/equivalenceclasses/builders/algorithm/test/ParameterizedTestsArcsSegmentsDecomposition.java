package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.test;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSegment;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.segmenting.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.segmenting.algorithm.ArcSegmentDecompositionAlgorithmByNgoEtAl;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable.AtomicSetActualError;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;

public class ParameterizedTestsArcsSegmentsDecomposition
{
    public static Stream<Arguments> parameters()
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
//            {0.8, Math.PI / 4.0, 3.0, 2.0, "8_"},
//            {0.3, Math.PI / 4.0, 3.0, 2.0, "9_"},
//            {0.3, Math.PI / 4.0, 3.0, 1.5, "10_"},
//            {0.3, Math.PI / 4.0, 3.0, 0.5, "11_"},
//            {0.3, Math.PI / 4.0, 3.0, 0.05, "12_"},
            {0.3, Math.PI / 4.0, 3.0, 1.0, 4.0, "13_"}, // -> best so far: don't prefer arcs over segments or the other way round, small 
//            {0.2, Math.PI / 4.0, 3.0, 1.2, "14_"},
            {0.2, Math.PI, 3.0, 1.2, 4.0, "15_"},
            {0.2, Math.PI / 2.0, 3.0, 1.2, 4.0,  "16_"},
            {0.2, Math.PI / 4.0, 3.0, 1.2, 4.0, "17_"},
            {0.2, Math.PI / 8.0, 3.0, 1.2, 4.0, "18_"}
        }).stream().map(params -> Arguments.of(params));
    }

    /**
     * 
     * @param thickness
     * @param alphaMax
     * @param nbCirclePoint
     * @param isseTol if bigger than 1 it favours arcs, smaller than 1 segments
     * @param maxRadius
     * @param suffix
     */
    @ParameterizedTest
    @MethodSource("parameters")
    public void testMultipleDecompositinosTrajectoryBeginningOfRoute(double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius, String suffix)
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
        slidingWindows = slidingWindows.subList(380, 700);
        String basePath = "./res/equivalencesegmentationtest/segmentationprogression_short/";
        Consumer<? super IndexAdder<List<TrajectoryElement>>> decompose = 
                curWindow -> decomposeWindow(curWindow.v(), curWindow.idx(), thickness, alphaMax, nbCirclePoint, isseTol, maxRadius,
                        basePath + "sampleWholeRoute" + suffix, basePath + "sampleWholeRouteTangentSpace" + suffix, basePath + "sampleWholeRouteSegmentation" + suffix);
        slidingWindows.stream().map(IndexAdder.indexed()).forEach(decompose);
    }
    
    private List<IArcsSegmentContainerElement> decomposeWindow(List<TrajectoryElement> curWindow, int curIdx, 
            double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius, 
            String routeFileName, String tangentSpaceFileName, String decompositionFileName)
    {
        String counter = String.format("%06d", curIdx);
        Deque<Vector2D> routeVectors = curWindow.stream().map(trajectory -> trajectory.getVector()).collect(Collectors.toCollection(LinkedList::new));
        List<String> routeAsString = routeVectors.stream().map(vec -> vec.getBase().toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File(routeFileName + counter + ".pyplot").toPath(), routeAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
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
        intermedidate = upperCtrl.getNewElements(10, AtomicSetActualError.INITIALIZATION_REQUEST);
        while(!intermedidate.isEmpty())
        {
            result.addAll(intermedidate);
            intermedidate = upperCtrl.getNewElements(10, AtomicSetActualError.INITIALIZATION_REQUEST);
        }
        return result;
    }
}
