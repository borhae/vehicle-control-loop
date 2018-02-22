package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.bezier.Spline2D;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectorizer;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehiclecontrol.Navigator;

public class SplineTest implements TestConstants
{

    @Test
    public void testNavigationRealMap()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        List<Line2D> downscaled = transform(route, 2.5f, -2000.0f, -2700.0f);
        
        Trajectorizer trajectorizer = new Trajectorizer();
        trajectorizer.createTrajectory(downscaled);
        Spline2D traversableSpline = trajectorizer.getTraversableSpline();

        JPanel panel = new TestOutPanel(trajectorizer.getPoints(), traversableSpline);
        panel.setPreferredSize(new Dimension(1920, 1080));
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

    private List<Line2D> transform(List<Line2D> route, float scale, float xTrans, float yTrans)
    {
        List<Line2D> result = new ArrayList<>();
        route.stream().forEach(line -> result.add(new Line2D((line.getX1() + xTrans)/scale, (line.getY1() + yTrans)/scale, (line.getX2() + xTrans)/scale, (line.getY2() + yTrans)/scale)));
        return result;
    }

}
