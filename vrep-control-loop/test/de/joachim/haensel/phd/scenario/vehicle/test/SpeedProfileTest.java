package de.joachim.haensel.phd.scenario.vehicle.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Route;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.Trajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterCircleIntersection;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;
import de.joachim.haensel.vehiclecontrol.Navigator;

public class SpeedProfileTest
{
    @Test
    public void testRealWorldOverlayedTrajectoriesAlignOriginal()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldNoTrains.net.xml");

        double maxVelocity = 30.0;
        double _segmentSize = 2.0;

        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(5747.01f, 2979.22f);
        Position2D destinationPosition = new Position2D(3031.06f, 4929.45f);
        List<Line2D> routeBasis = navigator.getRoute(startPosition, destinationPosition);
        ISegmenterFactory segmenterFactory = segmentSize -> new InterpolationSegmenterCircleIntersection(segmentSize);
        IVelocityAssignerFactory velocityAssignerFactory = segmentSize -> new BasicVelocityAssigner(segmentSize, maxVelocity);
        ITrajectorizer trajectorizer = new Trajectorizer(segmenterFactory, velocityAssignerFactory , _segmentSize);
        Route route = new Route();
        route.createRoute(trajectorizer.createTrajectory(routeBasis));
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("speed.csv", true));
            List<Trajectory> segments = route.getSegments(route.getSize());
            segments.forEach(t -> {
                try
                {
                    writer.append(t.toCSV());
                    writer.append(System.lineSeparator());
                }
                catch (IOException exc)
                {
                    exc.printStackTrace();
                }
            });
            writer.close();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("done");
    }
}
