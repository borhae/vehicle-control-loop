package de.joachim.haensel.phd.scenario.vehicle.test;


import static org.junit.Assert.assertEquals;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.Positioner;
import de.joachim.haensel.phd.scenario.navigation.visualization.SegmentBuildingAdapter;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterBinarySearch;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.OverlaySegmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.bezier.SegmenterBezier;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.bezier.Spline2D;

public class SegmentBuildingTest implements TestConstants
{
    @Test
    public void test3SyntheticVectorsSquareInterpolationIterative()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        input.add(new Vector2D(10, 0, 0, 10));
        input.add(new Vector2D(10, 10, -10, 0));

        OverlaySegmenter trajectorizer = new OverlaySegmenter(6, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);
        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(v));
        
        Deque<Vector2D> result = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, result, 6.0);
        result.stream().forEach(v -> checkValid(v));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(result, Color.BLACK, 1.0, 0.5);
        frame.addVectorSet(comparisonRoute, Color.BLUE, 1.0, 0.5);
        frame.setVisible(true);
        frame.updateVisuals();

        System.out.println("done");
    }
    
    @Test
    public void test3SyntheticVectorsSquareOverlayedTrajectoriesAlignQuantized()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        input.add(new Vector2D(10, 0, 0, 10));
        input.add(new Vector2D(10, 10, -10, 0));
        
        int stepSize = 6;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(v));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(quantizedRoute, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(quantizedRoute, Color.BLACK, 3.0, 0.05);
        frame.addVectorSet(comparisonRoute, Color.BLUE, 3.0, 0.05);
        frame.addVectorSet(overlayRoute, Color.ORANGE, 3.0, 0.05);
        frame.setVisible(true);
        frame.updateVisuals();
    }
    
    @Test
    public void test1SyntheticVectorOverlayedTrajectoriesAlignQuantized()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        
        int stepSize = 6;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(v));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(quantizedRoute, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(quantizedRoute, Color.BLACK, 3.0, 0.05);
        frame.addVectorSet(comparisonRoute, Color.BLUE, 3.0, 0.05);
        frame.addVectorSet(overlayRoute, Color.ORANGE, 3.0, 0.05);
        frame.setVisible(true);
        frame.updateVisuals();
    }
    
    @Test
    public void test2SyntheticVectorOverlayedTrajectoriesAlignQuantized()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        input.add(new Vector2D(10, 0, 0, 10));
        
        int stepSize = 6;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(v));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(quantizedRoute, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(quantizedRoute, Color.BLACK, 3.4, 0.05);
        frame.addVectorSet(comparisonRoute, Color.BLUE, 3.2, 0.05);
        frame.addVectorSet(overlayRoute, Color.ORANGE, 3.0, 0.05);
        frame.setVisible(true);
        frame.updateVisuals();
    }
 
    @Test
    public void test3SyntheticVectorsSquareOverlayedTrajectoriesAlignOriginal()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        input.add(new Vector2D(10, 0, 0, 10));
        input.add(new Vector2D(10, 10, -10, 0));
        
        int stepSize = 6;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        Deque<Vector2D> overlaySrc = new LinkedList<>();
        patchedRoute.stream().forEach(v -> overlaySrc.add(new Vector2D(v)));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(overlaySrc, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(comparisonRoute, Color.BLACK, new BasicStroke(3.0f));
        frame.addVectorSet(quantizedRoute, Color.BLUE, new BasicStroke(2.0f));
        frame.addVectorSet(overlayRoute, Color.ORANGE, new BasicStroke(1.0f));
        frame.setVisible(true);
        frame.updateVisuals();
    }
    
    @Test
    public void test1SyntheticVectorsSquareOverlayedTrajectoriesAlignOriginal()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        
        int stepSize = 6;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        Deque<Vector2D> overlaySrc = new LinkedList<>();
        patchedRoute.stream().forEach(v -> overlaySrc.add(new Vector2D(v)));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(overlaySrc, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(comparisonRoute, Color.BLACK, new BasicStroke(3.0f));
        frame.addVectorSet(quantizedRoute, Color.BLUE, new BasicStroke(2.0f));
        frame.addVectorSet(overlayRoute, Color.ORANGE, new BasicStroke(1.0f));
        frame.setVisible(true);
        frame.updateVisuals();
    }

    @Test
    public void test1SyntheticVectorsSquareOverlayedTrajectoriesAlignOriginalStepsizeTooLarge()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        
        int stepSize = 12;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        Deque<Vector2D> overlaySrc = new LinkedList<>();
        patchedRoute.stream().forEach(v -> overlaySrc.add(new Vector2D(v)));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(overlaySrc, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));

        Position2D lastTip = patchedRoute.peekLast().getTip();
        Position2D quantizedLastTip = quantizedRoute.peekLast().getTip();
        Position2D overlayLastTip = overlayRoute.peekLast().getTip();
        assert(quantizedRoute.size() == 1);
        assert(overlayRoute.size() == 2);
        assert(quantizedLastTip.equals(lastTip, 0.0000000000001));
        assert(overlayLastTip.equals(lastTip, 0.0000000000001));
       
        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(comparisonRoute, Color.BLACK, new BasicStroke(3.0f));
        frame.addVectorSet(quantizedRoute, Color.BLUE, new BasicStroke(2.0f));
        frame.addVectorSet(overlayRoute, Color.ORANGE, new BasicStroke(1.0f));
        frame.setVisible(true);
        frame.updateVisuals();
        System.out.println("wait");
    }
    
    @Test
    public void test1SyntheticVectorsSquareOverlayedTrajectoriesAlignOriginalStepsizeExactDouble()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 5.0, 0));
        input.add(new Vector2D(5.0, 0, 5.0, 0));
        
        int stepSize = 10;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        Deque<Vector2D> overlaySrc = new LinkedList<>();
        patchedRoute.stream().forEach(v -> overlaySrc.add(new Vector2D(v)));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(overlaySrc, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));

        Vector2D lastInInput = patchedRoute.peekLast();
        Vector2D lastInQuantized = quantizedRoute.peekLast();
        Vector2D lastInOverlay = overlayRoute.peekLast();
        
        Position2D lastTip = lastInInput.getTip();
        Position2D quantizedLastTip = lastInQuantized.getTip();
        Position2D overlayLastTip = lastInOverlay.getTip();

        assertEquals(1, quantizedRoute.size());
        assertEquals(2, overlayRoute.size());

        assertEquals(stepSize, lastInQuantized.length(), 0.0000000000001);
        assertEquals(lastInInput.length(), lastInOverlay.length(), 0.0000000000001);

        assert(quantizedLastTip.equals(lastTip, 0.0000000000001));
        assert(overlayLastTip.equals(lastTip, 0.0000000000001));
    }
    
    @Test
    public void test2SyntheticVectorsSquareOverlayedTrajectoriesAlignOriginalStepsizeExactSize()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10.0, 0));
        input.add(new Vector2D(10.0, 0, 0.0, 10.0));
        
        int stepSize = 10;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        Deque<Vector2D> overlaySrc = new LinkedList<>();
        patchedRoute.stream().forEach(v -> overlaySrc.add(new Vector2D(v)));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(overlaySrc, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));
        overlayRoute.stream().forEach(v -> checkValid(v));
        

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(patchedRoute, Color.BLACK, 5.0, 0.05);
        frame.addVectorSet(quantizedRoute, Color.BLUE, 4.0, 0.05);
        frame.addVectorSet(overlayRoute, Color.ORANGE, 3.0, 0.05);
        frame.setVisible(true);
        frame.updateVisuals();

        
        List<Vector2D> directAccessOverlay = new ArrayList<>(overlayRoute);

        Vector2D lastInInput = patchedRoute.peekLast();
        Vector2D lastInQuantized = quantizedRoute.peekLast();

        Vector2D firstInOverlay = directAccessOverlay.get(0);
        Vector2D middleInOverlay = directAccessOverlay.get(1);
        Vector2D lastInOverlay = directAccessOverlay.get(2);
        
        Position2D lastTip = lastInInput.getTip();
        Position2D quantizedLastTip = lastInQuantized.getTip();
        Position2D overlayLastTip = lastInOverlay.getTip();

        assert(quantizedRoute.size() == 2);
        assert(overlayRoute.size() == 3);

        assert(lastInQuantized.length() == lastInInput.length());
        assert(firstInOverlay.length() == stepSize / 2.0);
        assert(middleInOverlay.length() - stepSize < 0.00000001);
        
        assert(quantizedLastTip.equals(lastTip, 0.0000000000001));
        assert(overlayLastTip.equals(lastTip, 0.0000000000001));
    }
    
    @Test
    public void test2SyntheticVectorsSquareOverlayedTrajectoriesAlignOriginalStepsizeExactTwoThird()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 9.0, 0));
        input.add(new Vector2D(9.0, 0, 0.0, 9.0));
        
        int stepSize = 6;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        Deque<Vector2D> overlaySrc = new LinkedList<>();
        patchedRoute.stream().forEach(v -> overlaySrc.add(new Vector2D(v)));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(overlaySrc, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));
        overlayRoute.stream().forEach(v -> checkValid(v));
        

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(patchedRoute, Color.BLACK, 5.0, 0.05);
        frame.addVectorSet(quantizedRoute, Color.BLUE, 4.0, 0.05);
        frame.addVectorSet(overlayRoute, Color.ORANGE, 3.0, 0.05);
        frame.setVisible(true);
        frame.updateVisuals();


        Vector2D lastInInput = patchedRoute.peekLast();
        Vector2D lastInQuantized = quantizedRoute.peekLast();

        Vector2D firstInOverlay = overlayRoute.peek();
        Vector2D lastInOverlay = overlayRoute.peekLast();
        
        Position2D lastTip = lastInInput.getTip();
        Position2D quantizedLastTip = lastInQuantized.getTip();
        Position2D overlayLastTip = lastInOverlay.getTip();

        assert(quantizedRoute.size() == 3);
        assert(overlayRoute.size() == 4);

        assert(firstInOverlay.length() == stepSize / 2.0);
        assert(overlayRoute.pop().getLength() == (stepSize / 2.0)); 
        assert(overlayRoute.pop().getLength() == stepSize); 
        assert(overlayRoute.pop().getLength() == stepSize); 
        assert(overlayRoute.pop().getLength() == (stepSize / 2.0)); 
        
        assert(quantizedLastTip.equals(lastTip, 0.0000000000001));
        assert(overlayLastTip.equals(lastTip, 0.0000000000001));
    }
    
    @Test
    public void test2SyntheticVectorsSquareOverlayedTrajectoriesAlignOriginalStepsizeExactTwoThirdCircleIntersection()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 9.0, 0));
        input.add(new Vector2D(9.0, 0, 0.0, 9.0));
        
        int stepSize = 6;
        OverlaySegmenter trajectorizer = new OverlaySegmenter(stepSize, new InterpolationSegmenterCircleIntersection()); 

        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);

        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        Deque<Vector2D> overlaySrc = new LinkedList<>();
        patchedRoute.stream().forEach(v -> overlaySrc.add(new Vector2D(v)));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        Deque<Vector2D> overlayRoute = trajectorizer.createOverlay(overlaySrc, stepSize);
        quantizedRoute.stream().forEach(v -> checkValid(v));
        overlayRoute.stream().forEach(v -> checkValid(v));
        

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(patchedRoute, Color.BLACK, new BasicStroke(6.0f));
        frame.addVectorSet(quantizedRoute, Color.BLUE, new BasicStroke(4.0f));
        frame.addVectorSet(overlayRoute, Color.ORANGE, new BasicStroke(2.0f));
        frame.setVisible(true);
        frame.updateVisuals();


        Vector2D lastInInput = patchedRoute.peekLast();
        Vector2D lastInQuantized = quantizedRoute.peekLast();

        Vector2D firstInOverlay = overlayRoute.peek();
        Vector2D lastInOverlay = overlayRoute.peekLast();
        
        Position2D lastTip = lastInInput.getTip();
        Position2D quantizedLastTip = lastInQuantized.getTip();
        Position2D overlayLastTip = lastInOverlay.getTip();

        assert(quantizedRoute.size() == 3);
        assert(overlayRoute.size() == 4);

        assert(firstInOverlay.length() == stepSize / 2.0);
        assert(overlayRoute.pop().getLength() == (stepSize / 2.0)); 
        assert(overlayRoute.pop().getLength() == stepSize); 
        assert(overlayRoute.pop().getLength() == stepSize); 
        assert(overlayRoute.pop().getLength() == (stepSize / 2.0)); 
        
        assert(quantizedLastTip.equals(lastTip, 0.0000000000001));
        assert(overlayLastTip.equals(lastTip, 0.0000000000001));
    }

    @Test
    public void testRealWorldOverlayedSegmentsAlignOriginal()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        List<Line2D> downscaledRoute = transform(route, 2.5f, -2000.0f, -2700.0f);

        LinkedList<Vector2D> originalDownsacledVectorRoute = new LinkedList<>();
        downscaledRoute.stream().forEach(l -> originalDownsacledVectorRoute.add(new Vector2D(l)));
        
        OverlaySegmenter trajectorizer = new OverlaySegmenter(2, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(originalDownsacledVectorRoute);
        Deque<Vector2D> patchedRouteCopy = new LinkedList<>();
        patchedRoute.stream().forEachOrdered(v -> patchedRouteCopy.add(new Vector2D(v)));
        
        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        double stepSize = 2.0;
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        
        Deque<Vector2D> overlay = trajectorizer.createOverlay(patchedRouteCopy, stepSize);

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(patchedRoute, Color.BLACK, new BasicStroke(6.0f));
        frame.addVectorSet(quantizedRoute, Color.BLUE, new BasicStroke(4.0f));
        frame.addVectorSet(overlay, Color.ORANGE, new BasicStroke(2.0f));
        frame.setVisible(true);
        frame.updateVisuals();
        
        System.out.println("done");
        
    }
    
    @Test
    public void testRealWorldSegmentsNoOverlay()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        List<Line2D> downscaledRoute = transform(route, 2.5f, -2000.0f, -2700.0f);

        LinkedList<Vector2D> originalDownsacledVectorRoute = new LinkedList<>();
        downscaledRoute.stream().forEach(l -> originalDownsacledVectorRoute.add(new Vector2D(l)));
        
        Segmenter trajectorizer = new Segmenter(2, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(originalDownsacledVectorRoute);
        Deque<Vector2D> patchedRouteCopy = new LinkedList<>();
        patchedRoute.stream().forEachOrdered(v -> patchedRouteCopy.add(new Vector2D(v)));
        
        Deque<Vector2D> comparisonRoute = new LinkedList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(new Vector2D(v)));
        
        
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        double stepSize = 2.0;
        trajectorizer.quantize(patchedRoute, quantizedRoute, stepSize);
        
        Vector2DVisualizer frame = new Vector2DVisualizer();
//        frame.addVectorSet(comparisonRoute, Color.BLACK, 4, 0.05);
        frame.addVectorSet(quantizedRoute, Color.BLUE, 3, 0.05);
        frame.setVisible(true);
        frame.updateVisuals();
        
        System.out.println("done");
        
    }
    
    private void checkValid(Vector2D v)
    {
        double x = v.getBase().getX();
        double y = v.getBase().getY();
        double length = v.getLength();
        double dirX = v.getDir().getX();
        double dirY = v.getDir().getY();
        double normX = v.getNorm().getX();
        double normY = v.getNorm().getY();
        boolean invalid =
                Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(length) || Double.isNaN(dirX)  || Double.isNaN(dirY)  || Double.isNaN(normX)  || Double.isNaN(normY); 
        if(invalid)
        {
            throw new RuntimeException();
        }
    }

    @Test
    public void testSplineTrajectorizer()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        List<Line2D> downscaled = transform(route, 2.5f, -2000.0f, -2700.0f);
        
        double stepSize = 6.0;
        Segmenter trajectorizer = new Segmenter(stepSize, new SegmenterBezier());
        trajectorizer.createSegments(downscaled);
        Spline2D traversableSpline = ((SegmenterBezier)trajectorizer.getAlgorithm()).getTraversableSpline();

        System.out.println("done");
    }
    
    @Test
    public void testInterpolationTrajectorizerIterative()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        List<Line2D> downscaledRoute = transform(route, 2.5f, -2000.0f, -2700.0f);

        LinkedList<Vector2D> originalDownsacledVectorRoute = new LinkedList<>();
        downscaledRoute.stream().forEach(l -> originalDownsacledVectorRoute.add(new Vector2D(l)));
        
        OverlaySegmenter trajectorizer = new OverlaySegmenter(2, new InterpolationSegmenterBinarySearch()); 
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(originalDownsacledVectorRoute);
        List<Trajectory> trajectory = trajectorizer.createSegments(downscaledRoute);
        
        String length = trajectory.stream().map(trj -> "l: " + trj.getVector().getLength()).collect(Collectors.joining(System.lineSeparator()));
        
        Deque<Vector2D> quantizedRoute = new LinkedList<>();
        trajectory.stream().forEach(trj -> quantizedRoute.add(trj.getVector()));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.addVectorSet(quantizedRoute, Color.BLACK);
        frame.addVectorSet(patchedRoute, Color.RED);
        frame.setVisible(true);
        frame.updateVisuals();
        
        System.out.println(length);
        System.out.println("done");
    }
    
    @Test
    public void testVisualizationSingleVector()
    {
        Vector2DVisualizer frame = new Vector2DVisualizer();
        Deque<Vector2D> snippet = new LinkedList<>();
        snippet.add(new Vector2D(10.0f, 10.0f, 10.0f, 10.10f));
        snippet.add(new Vector2D(10.0f, 10.0f, 10.0f, 10.10f));
        frame.addVectorSet(snippet, Color.BLACK);
        frame.setVisible(true);
        System.out.println("wait!");
    }
 
    @Test
    public void testRouteOnSuperSimpleMapScaledDown()
    {
        float scaleFactor = 0.1f;
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        XYMinMax dimensions = roadMap.computeMapDimensions();
        double offX = dimensions.minX() + dimensions.distX() / 2.0;
        double offY = dimensions.minY() + dimensions.distY() / 2.0;
        offX *= scaleFactor;
        offY *= scaleFactor;

        TMatrix scaleOffsetMatrix = new TMatrix(scaleFactor, -offX, -offY);
        roadMap.transform(scaleOffsetMatrix);

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(-5.0f, 5.0f);
        Position2D destinationPosition = new Position2D(5.0f, -5.0f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);

        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX2(), lastLine.getY2());

        Vector2DVisualizer visualizer = new Vector2DVisualizer();
        visualizer.setVisible(true);
        System.out.println("stop");

        DefaultNavigationController nav = new DefaultNavigationController(2.0 * scaleFactor, 30.0);
        nav.addSegmentBuilderListener(new SegmentBuildingAdapter(visualizer));
        nav.initController(new Positioner(startingPoint), roadMap);
        nav.buildSegmentBuffer(destinationPosition, roadMap);
        Stream<Trajectory> segmentStream = nav.getNewSegments(nav.getSegmentBufferSize()).stream();
        Deque<Vector2D> segmentBufferAsVectors = segmentStream.map(traj -> traj.getVector()).collect(Collectors.toCollection(LinkedList::new));
    }

    private List<Line2D> transform(List<Line2D> route, float scale, float xTrans, float yTrans)
    {
        List<Line2D> result = new ArrayList<>();
        route.stream().forEach(line -> result.add(new Line2D((line.getX1() + xTrans)/scale, (line.getY1() + yTrans)/scale, (line.getX2() + xTrans)/scale, (line.getY2() + yTrans)/scale)));
        return result;
    }
}
