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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.ArcSegmentDecompositionAlgorithmByNgoEtAl;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSegment;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;

public class Test3NavigationRouteDecomposition
{
    public static Stream<Arguments> parameters()
    {
        return Arrays.asList(new Object[][]
        {
            {0.3, Math.PI / 4.0, 3.0, 1.0, 100000.0, new Position2D(5531.34,5485.96), new Position2D(5879.87,4886.08), "13_R1_"},
            {0.3, Math.PI / 4.0, 3.0, 1.0, 100000.0, new Position2D(6045.44, 2991.89), new Position2D(5867.1, 4934.41), "13_R2_"}, 
            {0.3, Math.PI / 4.0, 3.0, 1.0, 100000.0, new Position2D(2834.28, 4714.20), new Position2D(5809.49, 2938.12), "13_R3_"}, 
        }).stream().map(params -> Arguments.of(params));
    }

    /**
     * 
     * @param thickness
     * @param alphaMax
     * @param nbCirclePoint
     * @param isseTol if bigger than 1 it favours arcs, smaller than 1 segments
     * @param maxRadius
     * @param start
     * @param end
     * @param suffix
     */
    @ParameterizedTest
    @MethodSource("parameters")
    public void testARoute(double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius, Position2D start, Position2D end, String suffix)
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        TMatrix centerTransformMatrix = roadMap.center(0.0, 0.0);

        //uncentered positions measured by sumo net-edit tool
        Position2D startPosition = start;
        Position2D destinationPosition = end;
        
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
        String basePath = "./res/equivalencesegmentationtest/differentroutessegmentation/";
        
        Deque<Vector2D> routeVectors = allDataPoints.stream().map(trajectory -> trajectory.getVector()).collect(Collectors.toCollection(new Supplier<Deque<Vector2D>>() {
            @Override
            public Deque<Vector2D> get()
            {
                return new LinkedList<>();
            }
        }));
        List<String> routeAsString = routeVectors.stream().map(vec -> vec.getBase().toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File(basePath + "sampleWholeRoute" + suffix + ".pyplot").toPath(), routeAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
 
        
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
        intermedidate = upperCtrl.getNewElements(10);
        while(!intermedidate.isEmpty())
        {
            result.addAll(intermedidate);
            intermedidate = upperCtrl.getNewElements(10);
        }
        return result;
    }
}
