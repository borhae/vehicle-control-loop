package de.joachim.haensel.phd.scenario.vehicle.experiment.test;


import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.experiment.runner.ExperimentRunner;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class TestDrivingChallenges
{
    private static int _controlLoopRate;
    
    @BeforeAll
    public static void init()
    {
        _controlLoopRate = 120;
    }

    @Test
    public void testChallengingCurveRightActivating180Navigation()
    {
        System.out.println("running");
        try
        {
            ExperimentRunner runner = new ExperimentRunner();
            runner.initialize();
            List<Position2D> positions = Arrays.asList(new Position2D[] {new Position2D(6509.14,7979.43), new Position2D(6418.75,8070.73)});
            runner.run("luebeck_challengingcurve", 15.0, 120.0, 4.0, 4.3, 1.0, positions, "luebeck-roads.net.xml", "blue", _controlLoopRate);
            runner.tearDown();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("contemplate");
    }
    
    @Test
    public void testCloseToMapeEdgeBottom()
    {
        System.out.println("running");
        try
        {
            ExperimentRunner runner = new ExperimentRunner();
            runner.initialize();
            List<Position2D> positions = Arrays.asList(new Position2D[] {new Position2D(4558.70,57.26), new Position2D(4552.75,68.69)});
            runner.run("luebeck_challengingcurve", 15.0, 120.0, 4.0, 4.3, 1.0, positions, "luebeck-roads.net.xml", "blue", _controlLoopRate);
            runner.tearDown();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("contemplate");
    }
    
    @Test
    public void testCloseToMapeEdgeTop()
    {
        System.out.println("running");
        try
        {
            ExperimentRunner runner = new ExperimentRunner();
            runner.initialize();
            List<Position2D> positions = Arrays.asList(new Position2D[] {new Position2D(7700.91,9414.94), new Position2D(7696.58,9415.95)});
            runner.run("luebeck_challengingcurve", 15.0, 120.0, 4.0, 4.3, 1.0, positions, "luebeck-roads.net.xml", "blue", _controlLoopRate);
            runner.tearDown();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("contemplate");
    }
    
    @Test
    public void testCloseToMapeEdgeLeft()
    {
        System.out.println("running");
        try
        {
            ExperimentRunner runner = new ExperimentRunner();
            runner.initialize();
            List<Position2D> positions = Arrays.asList(new Position2D[] {new Position2D(112.38,1246.60), new Position2D(118.11,1234.28)});
            runner.run("luebeck_challengingcurve", 15.0, 120.0, 4.0, 4.3, 1.0, positions, "luebeck-roads.net.xml", "blue", _controlLoopRate);
            runner.tearDown();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("contemplate");
    }
    
    @Test
    public void testCloseToMapeEdgeRight()
    {
        System.out.println("running");
        try
        {
            ExperimentRunner runner = new ExperimentRunner();
            runner.initialize();
            List<Position2D> positions = Arrays.asList(new Position2D[] {new Position2D(10771.03,1414.03), new Position2D(10769.45,1420.05)});
            runner.run("luebeck_challengingcurve", 15.0, 120.0, 4.0, 4.3, 1.0, positions, "luebeck-roads.net.xml", "blue", _controlLoopRate);
            runner.tearDown();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        System.out.println("contemplate");
    }
}
