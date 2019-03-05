package de.joachim.haensel.phd.scenario.vehicle.experiment.test;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.experimentrunner.ExperimentRunner;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class TestDrivingChallenges
{
    @Test
    public void testChallengingCurveRightActivating180Navigation()
    {
        System.out.println("running");
        try
        {
            ExperimentRunner runner = new ExperimentRunner();
            runner.initialize();
            List<Position2D> positions = Arrays.asList(new Position2D[] {new Position2D(6509.14,7979.43), new Position2D(6418.75,8070.73)});
            runner.run("luebeck_challengingcurve", 15.0, 120.0, 4.0, 4.3, 1.0, positions, "luebeck-roads.net.xml", "blue");
            runner.tearDown();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("contemplate");
    }
}
