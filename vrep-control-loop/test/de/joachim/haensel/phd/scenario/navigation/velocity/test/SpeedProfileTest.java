package de.joachim.haensel.phd.scenario.navigation.velocity.test;



import static de.joachim.haensel.phd.scenario.vehicle.test.IsANumber.isANumber;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.SegmentBuffer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.ICurvatureChangeListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.IProfileChangeListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.GlodererHertleVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;

public class SpeedProfileTest
{
    private Integer _visualizationIdVelocities = null;
    private Integer _visualizationIdCurve = null;
    public static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";

    @Test
    public void testRealWorldSpeedprofileValidVelocities()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 3.0;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);
        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        for(int idx = 0; idx < trajectories.size(); idx++)
        {
            TrajectoryElement curTrajectory = trajectories.get(idx);
            double actualVelocity = curTrajectory.getVelocity();
//            assertThat("velocity should be a number (index: " + idx + ").", actualVelocity, isANumber());
            assertThat("velocity should be a number(index: " + idx + ").", actualVelocity, isANumber());
        }
        Deque<Vector2D> vectorSegments = trajectories.stream().map(t -> t.getVector()).collect(Collectors.toCollection(() -> new LinkedList<>()));
        frame.addVectorSet(vectorSegments, Color.BLACK, 1.0, 0.15);
        frame.updateVisuals();
    }

    @Test
    public void testLuebeckRoute8283GeneratedPositionsSeed4096() throws IOException
    {
        RoadMap roadMap = new RoadMap(RES_ROADNETWORKS_DIRECTORY + "luebeck-roads.net.xml");
        String pointsFileName = RES_ROADNETWORKS_DIRECTORY + "Luebeckpoints_generatedSeed4096.txt";
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        
        Stream<String> stream = Files.lines(Paths.get(pointsFileName)); 
        List<Position2D> positions = stream.map(stringPos -> new Position2D(stringPos)).collect(Collectors.toList());
        int routeAllTimeMaxIdx = positions.size();
        // 83 - 84 has an issue
        int minIdx = 81;
        int maxIdx = 82;
        if(minIdx >= maxIdx)
        {
            System.out.println("min idx larger or equal to max idx. returning.");
            return;
        }
        
        int routeStartIdx = Math.max(minIdx, 0);
        int routeEndIdx = Math.min(maxIdx, routeAllTimeMaxIdx);

        
        Position2D startPosition = positions.get(routeStartIdx).transform(scaleOffsetMatrix);
        Position2D destinationPosition = positions.get(routeEndIdx).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        double maxVelocity = 120.0;
        double segmentSize = 5.0;
        double maxLongAcc = 3.8;
        double maxLongDec = 4.0;
        double maxLateralAcc = 0.8;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);
        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        for(int idx = 0; idx < trajectories.size(); idx++)
        {
            TrajectoryElement curTrajectory = trajectories.get(idx);
            double actualVelocity = curTrajectory.getVelocity();
//            assertThat("velocity should be a number (index: " + idx + ").", actualVelocity, isANumber());
            assertThat("velocity should be a number(index: " + idx + ").", actualVelocity, isANumber());
        }
        Deque<Vector2D> vectorSegments = trajectories.stream().map(t -> t.getVector()).collect(Collectors.toCollection(() -> new LinkedList<>()));
        frame.addVectorSet(vectorSegments, Color.BLACK, 1.0, 0.15);
        frame.updateVisuals();
        System.out.println("enter anything and <enter> to finish");
        Scanner scanner = new Scanner(System.in);
        scanner.next();
        scanner.close();
    }

    @Test
    public void testLuebeckRoute94SpeedprofileValidVelocities()
    {
        RoadMap roadMap = new RoadMap(RES_ROADNETWORKS_DIRECTORY + "luebeck-roads.net.xml");
        TMatrix centerMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        
        List<String> pointsAsString = new ArrayList<String>();
        try
        {
            pointsAsString = Files.readAllLines(new File(RES_ROADNETWORKS_DIRECTORY + "Luebeckpoints_spread.txt").toPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        List<Position2D> allPositions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());
        //      List<Position2D> positions = allPositions.subList(0, allPositions.size());
        List<Position2D> rawPositions = allPositions.subList(93, 95);
        System.out.println(rawPositions.stream().map(point -> point.toString()).collect(Collectors.joining(", ")));
        List<Position2D> positions = rawPositions.stream().map(point -> point.transform(centerMatrix)).collect(Collectors.toList());
        System.out.println("Mapped targets:");
        System.out.println(positions.stream().map(point -> point.toString()).collect(Collectors.joining(", ")));

        
        Position2D startPosition = positions.get(0);
        Position2D destinationPosition = positions.get(1);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
//        120.0, 3.8, 4.0, 0.8
        double maxVelocity = 120.0;
        double segmentSize = 5.0;
        double maxLongAcc = 3.8;
        double maxLongDec = 4.0;
        double maxLateralAcc = 0.8;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);
        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        for(int idx = 0; idx < trajectories.size(); idx++)
        {
            TrajectoryElement curTrajectory = trajectories.get(idx);
            double actualVelocity = curTrajectory.getVelocity();
            assertThat("velocity should be a number(index: " + idx + ").", actualVelocity, isANumber());
        }
        Deque<Vector2D> vectorSegments = trajectories.stream().map(t -> t.getVector()).collect(Collectors.toCollection(() -> new LinkedList<>()));
        frame.addVectorSet(vectorSegments, Color.BLACK, 1.0, 0.15);
        frame.updateVisuals();
    }

    @Test
    public void testRealWorldSpeedprofileValidVelocities60KMpH()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = UnitConverter.kilometersPerHourToMetersPerSecond(60);
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 1.5;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);
        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        for(int idx = 0; idx < trajectories.size(); idx++)
        {
            TrajectoryElement curTrajectory = trajectories.get(idx);
            double actualVelocity = curTrajectory.getVelocity();
            assertThat("velocity should be a number (index: " + idx + ").", actualVelocity, isANumber());
        }
        Deque<Vector2D> vectorSegments = trajectories.stream().map(t -> t.getVector()).collect(Collectors.toCollection(() -> new LinkedList<>()));
        frame.addVectorSet(vectorSegments, Color.BLACK, 1.0, 0.15);
        frame.updateVisuals();
    }

    @Test
    public void testRealWorldSpeedprofileValidVelocities60KMpHGlodererHertleVelocityAssigner()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = UnitConverter.kilometersPerHourToMetersPerSecond(60);
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 1.5;

        ITrajectorizer trajectorizer = createTrajectorizerGlodererHertleAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);
        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        for(int idx = 0; idx < trajectories.size(); idx++)
        {
            TrajectoryElement curTrajectory = trajectories.get(idx);
            double actualVelocity = curTrajectory.getVelocity();
            assertThat("velocity should be a number (index: " + idx + ").", actualVelocity, isANumber());
        }
        Deque<Vector2D> vectorSegments = trajectories.stream().map(t -> t.getVector()).collect(Collectors.toCollection(() -> new LinkedList<>()));
        frame.addVectorSet(vectorSegments, Color.BLACK, 1.0, 0.15);
        frame.updateVisuals();
    }


    @Test
    public void testRealWorldSpeedprofileMaxVelocity()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 3.0;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        double expectedMaxVelocity = maxVelocity;
        for(int idx = 0; idx < trajectories.size(); idx++)
        {
            TrajectoryElement curTrajectory = trajectories.get(idx);
            double actualVelocity = curTrajectory.getVelocity();
            assertThat("velocity should be below " + expectedMaxVelocity + "(index: " + idx + ").", actualVelocity, lessThanOrEqualTo(expectedMaxVelocity));
        }
        Deque<Vector2D> vectorSegments = trajectories.stream().map(t -> t.getVector()).collect(Collectors.toCollection(() -> new LinkedList<>()));
        frame.addVectorSet(vectorSegments, Color.BLACK, 1.0, 0.15);
        frame.updateVisuals();
    }

    @Test
    public void testRealWorldSpeedprofileMaxLateralAcceleration()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 3.0;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        double expectedMaxLateralAcceleration = maxLateralAcc;
        for(int idx = 0; idx < trajectories.size() - 2; idx++)
        {
            TrajectoryElement t_0 = trajectories.get(idx);
            TrajectoryElement t_1 = trajectories.get(idx + 1);
            
            double v_0 = t_0.getVelocity();
            double v_1 = t_1.getVelocity();
            
            double meanVelocity = (v_0 + v_1) / 2.0;
            
            double vSqr = meanVelocity * meanVelocity;
            
            Vector2D mP_0 = t_0.getVector().getMiddlePerpendicular();
            Vector2D mP_1 = t_1.getVector().getMiddlePerpendicular();
            Double radius = Vector2D.scalarIntersect(mP_0, mP_1);
            double actualLateralVelocity = 0.0;
            if(radius == 0.0)
            {
            	fail("radius is zero, infinite acceleration (index: " + idx + ").");
            }
            if(!Double.isNaN(radius))
            {
                actualLateralVelocity = vSqr / radius;
                assertThat("lateral acceleration should stay in range (sqr velocity: " + vSqr + ", radius: " + radius +", at index: " + idx + ").", actualLateralVelocity, lessThanOrEqualTo(expectedMaxLateralAcceleration));
                
            }
            else
            {
                // infinite radius means zero lateral acceleration, we are fine here
            }
        }
    }


    @Test
    public void testRealWorldSpeedprofileSmallMaxLateralAcceleration()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 0.5;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        double expectedMaxLateralAcceleration = maxLateralAcc;
        for(int idx = 0; idx < trajectories.size() - 2; idx++)
        {
            TrajectoryElement t_0 = trajectories.get(idx);
            TrajectoryElement t_1 = trajectories.get(idx + 1);
            
            double v_0 = t_0.getVelocity();
            double v_1 = t_1.getVelocity();
            
            double meanVelocity = (v_0 + v_1) / 2.0;
            
            double vSqr = meanVelocity * meanVelocity;
            
            Vector2D mP_0 = t_0.getVector().getMiddlePerpendicular();
            Vector2D mP_1 = t_1.getVector().getMiddlePerpendicular();
            Double radius = Vector2D.scalarIntersect(mP_0, mP_1);
            double actualLateralVelocity = 0.0;
            if(radius == 0.0)
            {
                assertThat("radius is zero, infinite acceleration (index: " + idx + ").", false);
            }
            if(!Double.isNaN(radius))
            {
                actualLateralVelocity = vSqr / radius;
                assertThat("lateral acceleration should stay in range (sqr velocity: " + vSqr + ", radius: " + radius +", at index: " + idx + ").", actualLateralVelocity, lessThanOrEqualTo(expectedMaxLateralAcceleration));
                
            }
            else
            {
                // infinite radius means zero lateral acceleration, we are fine here
            }
        }
    }
    
    @Test
    public void testRealWorldSpeedprofileMaxLongitudalAcceleration()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 3.0;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        double expectedMaxLongitudinalAcceleration = maxLongAcc;

        for(int idx = 0; idx < trajectories.size() - 1; idx++)
        {
            TrajectoryElement t_0 = trajectories.get(idx);
            TrajectoryElement t_1 = trajectories.get(idx + 1);
            
            double v_0 = t_0.getVelocity();
            double v_1 = t_1.getVelocity();
            
            double distance = t_0.getVector().getBase().distance(t_1.getVector().getBase());
            double deltaT = distance / v_0;
            double deltaV = v_1 - v_0;
            double actualLongitudinalAcceleration = deltaV / deltaT;
            if(actualLongitudinalAcceleration > 0)
            {
                assertThat("acceleration should stay in range (index: " + idx + ").", actualLongitudinalAcceleration, lessThanOrEqualTo(expectedMaxLongitudinalAcceleration));
            }
        }
    }

    @Test
    public void testRealWorldSpeedprofileMaxLongitudalDeceleration()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0; // 30 m/s == 108 km/h
        double segmentSize = 5.0; // 2m
        double maxLongDec = 8.0; // 8 m^2/s
        double maxLongAcc = 2.0; // 2 m^2/s
        double maxLateralAcc = 3.0; // 3 m^2/s

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        double expectedMaxLongitudinalDeceleration = maxLongDec;
        for(int idx = 0; idx < trajectories.size() - 1; idx++)
        {
            TrajectoryElement t_0 = trajectories.get(idx);
            TrajectoryElement t_1 = trajectories.get(idx + 1);
            
            double v_0 = t_0.getVelocity();
            double v_1 = t_1.getVelocity();
            
            double distance = t_0.getVector().getBase().distance(t_1.getVector().getBase());
            double deltaT = distance / v_0;
            
            double deltaV = v_1 - v_0;
            double actualLongitudinalAcceleration = deltaV / deltaT;
            if(actualLongitudinalAcceleration < 0)
            {
                assertThat("deceleration should stay in range (index: " + idx + ").", actualLongitudinalAcceleration, greaterThanOrEqualTo(-expectedMaxLongitudinalDeceleration));
            }
        }
    }

    @Test
    public void testRealWorldSpeedprofileValidLongitudinalAccelerations()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 3.0;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        double sumOfAccelerationsDecelerations = 0.0;
        for(int idx = 0; idx < trajectories.size() - 1; idx++)
        {
            TrajectoryElement t_0 = trajectories.get(idx);
            TrajectoryElement t_1 = trajectories.get(idx + 1);
            
            double v_0 = t_0.getVelocity();
            double v_1 = t_1.getVelocity();
            
            double distance = t_0.getVector().getBase().distance(t_1.getVector().getBase());
            double deltaT = distance / v_0;
            double deltaV = v_1 - v_0;
            double currentLongitudinalAcceleration =  deltaV / deltaT;
            
            sumOfAccelerationsDecelerations += currentLongitudinalAcceleration;
            
            assertThat("overal acceleration should be a number (index: " + idx + ").", sumOfAccelerationsDecelerations, isANumber());
        }
        System.out.println("sum of accelerations and decelerations: " + sumOfAccelerationsDecelerations);
    }

    @Test
    public void testRealWorldSpeedprofileOutputAccelerationsDecelerations()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 3.0;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        for(int idx = 0; idx < trajectories.size() - 1; idx++)
        {
            TrajectoryElement t_0 = trajectories.get(idx);
            TrajectoryElement t_1 = trajectories.get(idx + 1);
            
            double v_0 = t_0.getVelocity();
            double v_1 = t_1.getVelocity();
            
            double distance = t_0.getVector().getBase().distance(t_1.getVector().getBase());
            double deltaT = distance / v_0;
            double deltaV = v_1 - v_0;
            double currentLongitudinalAcceleration =  deltaV / deltaT;
            
            systemOutData(idx, v_0, v_1, distance, deltaT, deltaV, currentLongitudinalAcceleration);
        }
    }

    private void systemOutData(int idx, double v_0, double v_1, double distance, double deltaT, double deltaV, double acc)
    {
        System.out.format("%4d: acc: : %06.3f, deltaV: %06.3f, deltaT: %06.3f, v_0: %06.3f, v_1: %06.3f, \n", idx, acc, deltaV, deltaT, v_0, v_1);
//        String v_0String = String.format("%06.3f", v_0);
//        System.out.println("distance: " + distance + ", v_0: " + v_0String + ", v_1: " + v_1 + ", deltaT: " + 
//                deltaT + ", deltaV: " + (v_1 - v_0) + ", acceleration: " + acc);
    }
   
    
    @Test
    public void testRealWorldSpeedprofileWillAlwaysMoveForward()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        TMatrix scaleOffsetMatrix = centerMap(roadMap);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f).transform(scaleOffsetMatrix);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f).transform(scaleOffsetMatrix);
        List<Line2D> lineRoute = navigator.getRoute(startPosition, destinationPosition);
        
        double maxVelocity = 30.0;
        double segmentSize = 5.0;
        double maxLongDec = 8.0;
        double maxLongAcc = 2.0;
        double maxLateralAcc = 3.0;

        ITrajectorizer trajectorizer = createTrajectorizerBasicVelocityAssigner(maxVelocity, segmentSize, maxLongDec, maxLongAcc, maxLateralAcc);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.showOnScreen(1);

        IProfileChangeListener listener = profile -> _visualizationIdVelocities = visualize(profile, frame, _visualizationIdVelocities);
        ICurvatureChangeListener curveListener = profile -> _visualizationIdCurve = visualizeCurvature(profile, frame, _visualizationIdCurve);
        trajectorizer.getVelocityAssigner().addProfileChangeListener(listener);
        trajectorizer.getVelocityAssigner().addCurvatureChangeListener(curveListener);
        frame.setVisible(true);
        frame.updateVisuals();
        
        SegmentBuffer route = new SegmentBuffer();
        route.fillBuffer(trajectorizer.createTrajectory(lineRoute));
        List<TrajectoryElement> trajectories = route.getSegments(route.getSize());
        
        for(int idx = 0; idx < trajectories.size(); idx++)
        {
            //last two segments are ok to have velocity 0 since we want to stop there right? :)
            if(idx < trajectories.size() - 2)
            {
                assertThat("speed should always be above zero (index: " + idx + ").", trajectories.get(idx).getVelocity(), greaterThan(0.0));
            }
        }
    }

    private ITrajectorizer createTrajectorizerBasicVelocityAssigner(double maxVelocity, double segmentSize, double maxLongDec, double maxLongAcc, double maxLateralAcc)
    {
        ISegmenterFactory segmenterFactory = segmentSizeParam -> new Segmenter(segmentSizeParam, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityAssignerFactory = segmentSizeParam -> new BasicVelocityAssigner(segmentSizeParam, maxVelocity, maxLateralAcc , maxLongAcc , maxLongDec);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityAssignerFactory , segmentSize);
        return trajectorizer;
    }

    private ITrajectorizer createTrajectorizerGlodererHertleAssigner(double maxVelocity, double segmentSize, double maxLongDec, double maxLongAcc, double maxLateralAcc)
    {
        ISegmenterFactory segmenterFactory = segmentSizeParam -> new Segmenter(segmentSizeParam, new InterpolationSegmenterCircleIntersection());
        IVelocityAssignerFactory velocityAssignerFactory = segmentSizeParam -> new GlodererHertleVelocityAssigner(segmentSizeParam, maxVelocity, maxLateralAcc , maxLongAcc , maxLongDec);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityAssignerFactory , segmentSize);
        return trajectorizer;
    }

    private TMatrix centerMap(RoadMap roadMap)
    {
        XYMinMax dimensions = roadMap.computeMapDimensions();
        double offX = dimensions.minX() + dimensions.distX()/2.0;
        double offY = dimensions.minY() + dimensions.distY()/2.0;

        TMatrix scaleOffsetMatrix = new TMatrix(1.0, -offX, -offY);
        roadMap.transform(scaleOffsetMatrix);
        return scaleOffsetMatrix;
    }

    private Integer visualizeCurvature(Deque<Vector2D> profile, Vector2DVisualizer frame, Integer id)
    {
        id = frame.addVectorSet(profile, Color.RED, 0.6, 0.07);
        frame.updateVisuals();
        return id;
    }

    private Integer visualize(List<TrajectoryElement> profile, Vector2DVisualizer frame, Integer id)
    {
        Deque<Vector2D> speedVectors = profile.stream().map(t -> (new Vector2D(t.getVector().getMiddlePerpendicular()).scaleNormTo(t.getVelocity()))).collect(Collectors.toCollection(() -> new LinkedList<>()));
        if(id == null)
        {
            id = frame.addVectorSet(speedVectors, Color.BLUE, 0.5, 0.05);
        }
        else
        {
            frame.updateContentElement(id, speedVectors);
        }
        frame.updateVisuals();
        return id;
    }
}
