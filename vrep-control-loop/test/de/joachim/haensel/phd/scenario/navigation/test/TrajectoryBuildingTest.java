package de.joachim.haensel.phd.scenario.navigation.test;

import static org.junit.Assert.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.bezier.Spline2D;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.visualization.TestOutPanel;
import de.joachim.haensel.phd.scenario.navigation.visualization.TrajectorySnippetFrame;
import de.joachim.haensel.phd.scenario.navigation.visualization.Vector2DVisualizer;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.navigation.AbstractTrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.InterpolationTrajectorizerTrigonometry;
import de.joachim.haensel.phd.scenario.vehicle.navigation.IterativeInterpolationTrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.SplineTrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehiclecontrol.Navigator;

public class TrajectoryBuildingTest implements TestConstants
{
    @Test
    public void testTransformLineToVectorNotANumber()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        AbstractTrajectorizer trajectorizer = new InterpolationTrajectorizerTrigonometry(5);
        LinkedList<Vector2D> lineListToVectorList = trajectorizer.lineListToVectorList(route);
        lineListToVectorList.stream().forEach(v -> checkValid(v));
    }
    
    @Test
    public void testFillEmptyPartsNotNotANumber()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        AbstractTrajectorizer trajectorizer = new InterpolationTrajectorizerTrigonometry(5);
        LinkedList<Vector2D> lineListToVectorList = trajectorizer.lineListToVectorList(route);
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(lineListToVectorList);
        patchedRoute.stream().forEach(v -> checkValid(v));
    }
    
    @Test
    public void testMinimalDistance()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        AbstractTrajectorizer trajectorizer = new InterpolationTrajectorizerTrigonometry(5);
        LinkedList<Vector2D> lineListToVectorList = trajectorizer.lineListToVectorList(route);
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(lineListToVectorList);
        patchedRoute.stream().forEach(v -> checkValid(v));
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for(int idx = 0; idx + 1 < patchedRoute.size(); idx++)
        {
            double distance = Position2D.distance(patchedRoute.get(idx).getTip(), patchedRoute.get(idx + 1).getBase());
            if(distance < min)
            {
                min = distance;
            }
            else if(distance > max)
            {
                max = distance;
            }
        }
        assertTrue(min >= 0);
        assertTrue(max <= 0.0000000000000000000000000001);
        System.out.println("Min: " + min + ", max: " + max);
    }
    
    @Test
    public void checkMinimalDistanceUnpatched()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        AbstractTrajectorizer trajectorizer = new InterpolationTrajectorizerTrigonometry(15);
        LinkedList<Vector2D> routeAsVectors = trajectorizer.lineListToVectorList(route);
        routeAsVectors.stream().forEach(v -> checkValid(v));
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        List<Double> distances = new ArrayList<>();
        for(int idx = 0; idx + 1 < routeAsVectors.size(); idx++)
        {
            double distance = Position2D.distance(routeAsVectors.get(idx).getTip(), routeAsVectors.get(idx + 1).getBase());
            if(distance < min)
            {
                min = distance;
            }
            else if(distance > max)
            {
                max = distance;
            }
            if(distance > 0.000000000000001)
            {
                distances.add(new Double(distance));
            }
        }
        System.out.println("Min: " + min + ", max: " + max);
        System.out.println(distances);
        System.out.println(distances.size());
    }
    
    @Test
    public void checkLengths()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        AbstractTrajectorizer trajectorizer = new InterpolationTrajectorizerTrigonometry(15);
        LinkedList<Vector2D> routeAsVectors = trajectorizer.lineListToVectorList(route);
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(routeAsVectors);
        routeAsVectors.stream().forEach(v -> checkValid(v));
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        List<Double> smallLengths = new ArrayList<>();
        for(int idx = 0; idx < patchedRoute.size(); idx++)
        {
            double length = patchedRoute.get(idx).getLength();
            if(length < min)
            {
                min = length;
            }
            else if(length > max)
            {
                max = length;
            }
            if(length < 0.1)
            {
                smallLengths.add(new Double(length));
            }
        }
        System.out.println("Min: " + min + ", max: " + max);
        System.out.println(smallLengths);
        System.out.println(smallLengths.size());
    }

    @Test 
    public void test3SyntheticVectorsSquareInterpolationTrigonometry()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        input.add(new Vector2D(10, 0, 0, 10));
        input.add(new Vector2D(10, 10, -10, 0));
        
        InterpolationTrajectorizerTrigonometry trajecorizer = new InterpolationTrajectorizerTrigonometry(6);
        LinkedList<Vector2D> patchedRoute = trajecorizer.patchHolesInRoute(input);
        List<Vector2D> result = new LinkedList<>();
        trajecorizer.interpolateRecursiveNonWorking(patchedRoute, null, result, 6);
        result.stream().forEach(v -> checkValid(v));
    }
    
    @Test
    public void test3SyntheticVectorsSquareInterpolationIterative()
    {
        LinkedList<Vector2D> input = new LinkedList<>();
        input.add(new Vector2D(0, 0, 10, 0));
        input.add(new Vector2D(10, 0, 0, 10));
        input.add(new Vector2D(10, 10, -10, 0));

        IterativeInterpolationTrajectorizer trajectorizer = new IterativeInterpolationTrajectorizer(6);
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(input);
        List<Vector2D> comparisonRoute = new ArrayList<>();
        patchedRoute.stream().forEach(v -> comparisonRoute.add(v));
        
        ArrayList<Vector2D> result = new ArrayList<>();
        trajectorizer.quantize(patchedRoute, result, 6);
        result.stream().forEach(v -> checkValid(v));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.setVectors(result);
        frame.setBlueVectors(comparisonRoute);
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
        
        ITrajectorizer trajectorizer = new SplineTrajectorizer();
        trajectorizer.createTrajectory(downscaled);
        Spline2D traversableSpline = ((SplineTrajectorizer)trajectorizer).getTraversableSpline();

        float scale = 1.5f;
        System.out.println("done");
    }
    
    @Test
    public void testInterpolationTrajectorizerTrigonometry()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        List<Line2D> downscaledRoute = transform(route, 2.5f, -2000.0f, -2700.0f);

        LinkedList<Vector2D> originalDownsacledVectorRoute = new LinkedList<>();
        downscaledRoute.stream().forEach(l -> originalDownsacledVectorRoute.add(new Vector2D(l)));
        
        InterpolationTrajectorizerTrigonometry trajectorizer = new InterpolationTrajectorizerTrigonometry(5);
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(originalDownsacledVectorRoute);
        List<Trajectory> trajectory = trajectorizer.createTrajectory(downscaledRoute);
        
        String length = trajectory.stream().map(trj -> "l: " + trj.getVector().getLength()).collect(Collectors.joining(System.lineSeparator()));
        
        List<Vector2D> quantizedRoute = new ArrayList<>();
        trajectory.stream().forEach(trj -> quantizedRoute.add(trj.getVector()));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.setVectors(quantizedRoute);
        frame.setBlueVectors(patchedRoute);
        frame.setVisible(true);
        frame.updateVisuals();
        
        System.out.println(length);
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
        
        IterativeInterpolationTrajectorizer trajectorizer = new IterativeInterpolationTrajectorizer(2);
        LinkedList<Vector2D> patchedRoute = trajectorizer.patchHolesInRoute(originalDownsacledVectorRoute);
        List<Trajectory> trajectory = trajectorizer.createTrajectory(downscaledRoute);
        
        String length = trajectory.stream().map(trj -> "l: " + trj.getVector().getLength()).collect(Collectors.joining(System.lineSeparator()));
        
        List<Vector2D> quantizedRoute = new ArrayList<>();
        trajectory.stream().forEach(trj -> quantizedRoute.add(trj.getVector()));

        Vector2DVisualizer frame = new Vector2DVisualizer();
        frame.setVectors(quantizedRoute);
//        frame.setBlueVectors(patchedRoute);
        frame.setVisible(true);
        frame.updateVisuals();
        
        System.out.println(length);
        System.out.println("done");
    }
    
    @Test
    public void testVisualizationSingleVector()
    {
        Vector2DVisualizer frame = new Vector2DVisualizer();
        ArrayList<Vector2D> snippet = new ArrayList<>();
        snippet.add(new Vector2D(10.0f, 10.0f, 10.0f, 10.10f));
        snippet.add(new Vector2D(10.0f, 10.0f, 10.0f, 10.10f));
        frame.setVectors(snippet);
        frame.setVisible(true);
        System.out.println("wait!");
    }
    
    private List<Line2D> transform(List<Line2D> route, float scale, float xTrans, float yTrans)
    {
        List<Line2D> result = new ArrayList<>();
        route.stream().forEach(line -> result.add(new Line2D((line.getX1() + xTrans)/scale, (line.getY1() + yTrans)/scale, (line.getX2() + xTrans)/scale, (line.getY2() + yTrans)/scale)));
        return result;
    }
}
