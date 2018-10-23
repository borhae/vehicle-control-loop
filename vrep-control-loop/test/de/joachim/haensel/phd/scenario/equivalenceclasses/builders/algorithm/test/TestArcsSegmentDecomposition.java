package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.ArcSegmentDecompositionAlgorithmByNgoEtAl;
import de.joachim.haensel.phd.scenario.layerinterface.RandomMapPositionCreator;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Midpoint;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSegment;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceMidpointComputer;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.navigation.visualization.ArcSegmentContentElement;
import de.joachim.haensel.phd.scenario.navigation.visualization.IContentElement;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.navigation.visualization.VectorContentElement;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.streamextensions.IndexAdder;

public class TestArcsSegmentDecomposition
{
    @Test
    public void testTangentSpaceCreationFrom2DPointsPaperExample()
    {
        List<Position2D> dataPoints = new ArrayList<>();
        try
        {
            //load arcs and segments defined by coordinates from file into list  
            Path path = new File("./res/equivalencesegmentationtest/sampleArc.txt").toPath();
            Files.lines(path).forEachOrdered(line -> dataPoints.add(new Position2D(line, " ")));
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        List<String> tangentSpaceFileContent = TangentSpaceTransformer.tangentSpaceAsMatplotFile(tangentSpace);
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleArcTangentSpace.txt").toPath(), tangentSpaceFileContent, Charset.defaultCharset(), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed");
    }

    @Test
    public void testTangentSpaceCreationAndMidpointComputationSimpleArcPaperExample()
    {
        List<Position2D> dataPoints = new ArrayList<>();
        try
        {
            //load arcs and segments defined by coordinates from file into list  
            Path path = new File("./res/equivalencesegmentationtest/sampleArc.txt").toPath();
            Files.lines(path).forEachOrdered(line -> dataPoints.add(new Position2D(line, " ")));
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        List<Midpoint> midPoints = TangentSpaceMidpointComputer.compute(tangentSpace);
        List<String> midPointsAsString = midPoints.stream().map(point -> "point " + point.toString(" ")).collect(Collectors.toList());
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleArcMidpoints.txt").toPath(), midPointsAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed");
    }

    @Test
    public void testTangentSpaceCreationFrom2DPoints()
    {
        List<Position2D> dataPoints = new ArrayList<>();
        try
        {
            //load a circle defined by coordinates from file into list  
            Path path = new File("./res/equivalencesegmentationtest/circle.dat").toPath();
            Files.lines(path).forEachOrdered(line -> dataPoints.add(new Position2D(line)));
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        List<String> tangentSpaceFileContent = TangentSpaceTransformer.tangentSpaceAsFile(tangentSpace, ", ");
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/circleTangentSpace.dat").toPath(), tangentSpaceFileContent, Charset.defaultCharset(), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed");
    }

    @Test
    public void testSegmentationFromSimpleArcPaperExample()
    {
        List<Position2D> dataPoints = new ArrayList<>();
        try
        {
            //load a circle defined by coordinates from file into list  
            Path path = new File("./res/equivalencesegmentationtest/sampleArcAdditionalPoints.txt").toPath();
            Files.lines(path).forEachOrdered(line -> dataPoints.add(new Position2D(line, " ")));
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(dataPoints);
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        List<Midpoint> midPoints = TangentSpaceMidpointComputer.compute(tangentSpace);
        
        
        List<String> tangentSpaceFileContent = TangentSpaceTransformer.tangentSpaceAsFile(tangentSpace, " ");
        List<String> midPointsAsString = midPoints.stream().map(point -> point.toString(" ")).collect(Collectors.toList());
        List<String> elementsAsString = segments.stream().map(element -> element.toGnuPlotString()).collect(Collectors.toList());
        List<String> tangentAndMidpointString = new ArrayList<>();
        for(int idx = 0; idx < tangentSpaceFileContent.size(); idx++)
        {
            String curTang = tangentSpaceFileContent.get(idx);
            String curMid = "";
            if(idx < midPointsAsString.size())
            {
                curMid = midPointsAsString.get(idx);
            }
            tangentAndMidpointString.add(curTang + " " + curMid);
        }
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleArcAdditionalPointsTangentsMidpoints.dat").toPath(), tangentAndMidpointString, Charset.defaultCharset(), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleArcAdditionalPointsResult.dat").toPath(), elementsAsString, Charset.defaultCharset(), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }

        System.out.println("finshed");
    }
    

    @Test
    public void segmentationFromSimpleArcPaperExample()
    {
        List<Position2D> dataPoints = new ArrayList<>();
        try
        {
            //load a circle defined by coordinates from file into list  
            Path path = new File("./res/equivalencesegmentationtest/sampleArcAdditionalPoints.txt").toPath();
            Files.lines(path).forEachOrdered(line -> dataPoints.add(new Position2D(line, " ")));
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(dataPoints);
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        List<Midpoint> midPoints = TangentSpaceMidpointComputer.compute(tangentSpace);
        
        
        Deque<Vector2D> tangentSpaceVisuals = new LinkedList<>();
        Position2D tn2_last = new Position2D(0.0, 0.0);
        double maxX = 0.0;
        double maxY = 0.0;
        for (TangentSegment curSeg : tangentSpace)
        {
            Position2D tn1 = curSeg.getTn1();
            Position2D tn2 = curSeg.getTn2();
            if(tn1 != null)
            {
                tangentSpaceVisuals.add(new Vector2D(tn2_last, tn1));
                if(tn2 != null)
                {
                    tangentSpaceVisuals.add(new Vector2D(tn1, tn2));
                }
            }
            tn2_last = tn2;
        }

        List<String> elementsAsString = segments.stream().map(element -> element.toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleArcAdditionalPointsResult.pyplot").toPath(), elementsAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed");
    }

    @Test
    public void visualizeDecompositionFromTwoFollowUpStraightLines()
    {
        List<Position2D> dataPoints = new ArrayList<>();
        dataPoints.add(new Position2D(10.0, 10.0));
        dataPoints.add(new Position2D(20.0, 10.0));
        dataPoints.add(new Position2D(30.0, 10.0));
        dataPoints.add(new Position2D(40.0, 10.0));
        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(dataPoints);
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        
        Deque<Vector2D> tangentSpaceVisuals = new LinkedList<>();
        Position2D tn2_last = new Position2D(0.0, 0.0);
        for (TangentSegment curSeg : tangentSpace)
        {
            Position2D tn1 = curSeg.getTn1();
            Position2D tn2 = curSeg.getTn2();
            if(tn1 != null)
            {
                tangentSpaceVisuals.add(new Vector2D(tn2_last, tn1));
                if(tn2 != null)
                {
                    tangentSpaceVisuals.add(new Vector2D(tn1, tn2));
                }
            }
            tn2_last = tn2;
        }

        Vector2DVisualizer vis = new Vector2DVisualizer();
        IContentElement visualizee = new ArcSegmentContentElement(segments, Color.BLACK, new BasicStroke(1.0f));
        vis.addContentElement(visualizee);
        vis.setVisible(true);
        vis.centerContent();
        IContentElement refVisualization = new VectorContentElement(new LinkedList<>(Arrays.asList(new Vector2D[]{new Vector2D(10.0, 12.0, 30.0, 0.0)})), Color.BLUE, new BasicStroke(1.0f));
        vis.addContentElement(refVisualization);
        vis.centerContent();
        vis.updateVisuals();
        System.out.println("finshed");
    }


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

        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(dataPoints);
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
        
        List<List<Trajectory>> slidingWindows = createSlidingWindows(allDataPoints, 100, 5);
        Deque<Deque<Vector2D>> slidingWindowsVectors = transformToVectorDeque(slidingWindows);

        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        
        Deque<Vector2D> firstWindow = slidingWindowsVectors.getFirst();
        VectorContentElement visualizee = new VectorContentElement(firstWindow, Color.BLACK, new BasicStroke(4.0f), 0.1);
        visualizer.addVectorSet(firstWindow, Color.BLACK, 4.0, 0.02);
        List<List<Trajectory>> data = new ArrayList<>();
        data.add(allDataPoints);
        
        visualizer.addContentElement(visualizee);
        visualizer.setVisible(true);
        visualizer.updateVisuals();
        visualizee.addVector(new Vector2D(10, 30, 20, 40));
        visualizer.centerContent();
        visualizer.updateVisuals();

        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(slidingWindowsVectors.getFirst());
        ArcSegmentContentElement segmentVisualizee = new ArcSegmentContentElement(segments, Color.BLUE, new BasicStroke(3.0f));
        visualizer.addContentElement(segmentVisualizee);
        visualizer.updateVisuals();
        System.out.println("number of datapoints: " + allDataPoints.size() + System.lineSeparator() + segments);
    }
    
    @Test
    public void testDecompositionTrajectoryBigWindow1()
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
        
        List<Trajectory> allDataPoints = getDataPoints(upperCtrl);
        
        List<List<Trajectory>> slidingWindows = createSlidingWindows(allDataPoints, 200, 5);
        Deque<Deque<Vector2D>> slidingWindowsVectors = transformToVectorDeque(slidingWindows);

        Deque<Vector2D> firstWindow = slidingWindowsVectors.getFirst();
        List<String> routeAsString = firstWindow.stream().map(vec -> vec.getBase().toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleRouteBigFirstWindow.pyplot").toPath(), routeAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing route");

        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(firstWindow);
        List<String> elementsAsString = segments.stream().map(element -> element.toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleRouteBigFirstWindowSegmentation.pyplot").toPath(), elementsAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing segmentation");

        System.out.println("number of datapoints: " + allDataPoints.size() + System.lineSeparator() + segments);
    }
    
    @Test
    public void testDecompositionTrajectorySmallWindow1()
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
        
        List<Trajectory> allDataPoints = getDataPoints(upperCtrl);
        
        List<List<Trajectory>> slidingWindows = createSlidingWindows(allDataPoints, 50, 5);
        Deque<Deque<Vector2D>> slidingWindowsVectors = transformToVectorDeque(slidingWindows);

        Deque<Vector2D> firstWindow = slidingWindowsVectors.getFirst();
        List<String> routeAsString = firstWindow.stream().map(vec -> vec.getBase().toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleRouteSmallFirstWindow.pyplot").toPath(), routeAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing route");

        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(firstWindow);
        List<String> elementsAsString = segments.stream().map(element -> element.toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleRouteSmallFirstWindowSegmentation.pyplot").toPath(), elementsAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing segmentation");

        System.out.println("number of datapoints: " + allDataPoints.size() + System.lineSeparator() + segments);
    }
    
    @Test
    public void testDecompositionTrajectoryTinyWindow1()
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
        
        List<Trajectory> allDataPoints = getDataPoints(upperCtrl);
        
        List<List<Trajectory>> slidingWindows = createSlidingWindows(allDataPoints, 10, 5);
        Deque<Deque<Vector2D>> slidingWindowsVectors = transformToVectorDeque(slidingWindows);

        Deque<Vector2D> firstWindow = slidingWindowsVectors.getFirst();
        List<String> routeAsString = firstWindow.stream().map(vec -> vec.getBase().toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleRouteTinyFirstWindow.pyplot").toPath(), routeAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing route");

        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(firstWindow);
        List<String> elementsAsString = segments.stream().map(element -> element.toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleRouteTinyFirstWindowSegmentation.pyplot").toPath(), elementsAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing segmentation");

        System.out.println("number of datapoints: " + allDataPoints.size() + System.lineSeparator() + segments);
    }
    
    @Test
    public void testDecompositionTrajectoryWholeRoute()
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
        
        List<Trajectory> allDataPoints = getDataPoints(upperCtrl);
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
            Files.write(new File("./res/equivalencesegmentationtest/sampleWholeRoute.pyplot").toPath(), routeAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing route");
        
        List<TangentSegment> tangentSpaceRoute = TangentSpaceTransformer.transform(routeVectors);
        List<String> tangentSpaceAsString = TangentSpaceTransformer.tangentSpaceAsMatplotFile(tangentSpaceRoute);
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleWholeRouteTangentSpace.pyplot").toPath(), tangentSpaceAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing segmentation");
        
        ArcSegmentDecompositionAlgorithmByNgoEtAl segmenter = new ArcSegmentDecompositionAlgorithmByNgoEtAl();
        List<IArcsSegmentContainerElement> segments = segmenter.createSegments(routeVectors);
        List<String> elementsAsString = segments.stream().map(element -> element.toPyPlotString()).collect(Collectors.toList());
        
        try
        {
            Files.write(new File("./res/equivalencesegmentationtest/sampleWholeRouteSegmentation.pyplot").toPath(), elementsAsString, Charset.defaultCharset());
        }
        catch (IOException exc)
        {
            // TODO Auto-generated catch block
            exc.printStackTrace();
        }
        System.out.println("finshed writing segmentation");

        System.out.println("number of datapoints: " + allDataPoints.size() + System.lineSeparator() + segments);
    }
    
    @Test
    public void testMultipleDecompositinosTrajectoryWholeRoute()
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
        
        List<Trajectory> allDataPoints = getDataPoints(upperCtrl);
        
        int windowSize = 30;
        List<List<Trajectory>> slidingWindows = createSlidingWindows(allDataPoints, windowSize, allDataPoints.size() / windowSize);
        double thickness = 1.5;
        double alphaMax = Math.PI / 4.0;
        double nbCirclePoint = 3;
        double isseTol = 4.0;
        double maxRadius = 100000;
        
        slidingWindows.stream().map(IndexAdder.indexed()).forEach(curWindow -> decomposeWindow(curWindow.v(), curWindow.idx(), thickness, alphaMax, nbCirclePoint, isseTol, maxRadius, "./res/equivalencesegmentationtest/segmentationprogression/sampleWholeRoute", "./res/equivalencesegmentationtest/segmentationprogression/sampleWholeRouteTangentSpace", "./res/equivalencesegmentationtest/segmentationprogression/sampleWholeRouteSegmentation"));
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
        
        List<Trajectory> allDataPoints = getDataPoints(upperCtrl);
        
        int windowSize = 30;
        List<List<Trajectory>> slidingWindows = createSlidingWindows(allDataPoints, windowSize, allDataPoints.size() / windowSize);
        double thickness = 1.5;
        double alphaMax = Math.PI / 4.0;
        double nbCirclePoint = 3;
        double isseTol = 4.0;
        double maxRadius = 100000;
        slidingWindows = slidingWindows.subList(0, 4);
        String basePath = "./res/equivalencesegmentationtest/segmentationprogression_short/";
        Consumer<? super IndexAdder<List<Trajectory>>> decompose = 
                curWindow -> decomposeWindow(curWindow.v(), curWindow.idx(), thickness, alphaMax, nbCirclePoint, isseTol, maxRadius,
                        basePath + "sampleWholeRoute", basePath + "sampleWholeRouteTangentSpace", basePath + "sampleWholeRouteSegmentation");
        slidingWindows.stream().map(IndexAdder.indexed()).forEach(decompose);
    }

    private List<IArcsSegmentContainerElement> decomposeWindow(List<Trajectory> curWindow, int curIdx, 
            double thickness, double alphaMax, double nbCirclePoint, double isseTol, 
            double maxRadius, String routeFileName, String tangentSpaceFileName, String decompositionFileName)
    {
        String counter = String.format("%03d", curIdx);
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

    private Deque<Deque<Vector2D>> transformToVectorDeque(List<List<Trajectory>> slidingWindows)
    {
        Deque<Deque<Vector2D>> result = new LinkedList<>();
        slidingWindows.forEach(curWindow -> result.addLast(new LinkedList<>(trajectoryToVector2DList(curWindow))));
        return result;
    }

    private List<Vector2D> trajectoryToVector2DList(List<Trajectory> curWindow)
    {
        return curWindow.stream().map(t -> t.getVector()).collect(Collectors.toList());
    }

    private List<List<Trajectory>> createSlidingWindows(List<Trajectory> allDataPoints, int windowSize, int amount)
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
