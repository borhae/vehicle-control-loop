package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.bezier.Spline2D;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.visualization.TestOutPanel;
import de.joachim.haensel.phd.scenario.navigation.visualization.TrajectorySnippetFrame;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.InterpolationTrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.SplineTrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehiclecontrol.Navigator;

public class TrajectoryBuildingTest implements TestConstants
{

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
        JPanel panel = new TestOutPanel(trajectorizer.getPoints(), traversableSpline, scale);
        panel.setPreferredSize(new Dimension(2560, 1440));
        JFrame frame = new JFrame();
        JPanel cp = (JPanel) frame.getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.out.println("done");
    }
    
    @Test
    public void testInterpolationTrajectorizer()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        List<Line2D> downscaled = transform(route, 2.5f, -2000.0f, -2700.0f);
        
        ITrajectorizer trajectorizer = new InterpolationTrajectorizer();
        List<Trajectory> trajectory = trajectorizer.createTrajectory(downscaled);
        
        float scale = 1.2f;
        JPanel panel = new TestOutPanel(trajectorizer.getPoints(), trajectory, scale);
        panel.setPreferredSize(new Dimension(2560, 1440));
        JFrame frame = new JFrame();
        JPanel cp = (JPanel) frame.getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String length = trajectory.stream().map(trj -> "l: " + trj.getVector().length()).collect(Collectors.joining(System.lineSeparator()));
        System.out.println(length);
        System.out.println("done");
    }
    
    @Test
    public void testVisualizationSingleVector()
    {
        TrajectorySnippetFrame frame = new TrajectorySnippetFrame();
        ArrayList<Vector2D> snippet = new ArrayList<>();
        snippet.add(new Vector2D(10.0f, 10.0f, 10.0f, 10.10f));
        snippet.add(new Vector2D(10.0f, 10.0f, 10.0f, 10.10f));
        frame.setCurRoute(snippet, snippet.get(0));
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
