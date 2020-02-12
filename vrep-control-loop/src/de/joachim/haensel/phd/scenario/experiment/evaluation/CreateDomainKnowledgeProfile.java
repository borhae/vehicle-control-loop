package de.joachim.haensel.phd.scenario.experiment.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.math.Linspace;

public class CreateDomainKnowledgeProfile
{
    public static void main(String[] args)
    {
        double[][] trajectorySlowStraight = createTrajectorySlowStraight();
        double[][] trajectoryMediumFastStraight = createTrajectoryMediumFastStraight();
        double[][] trajectoryFastStraight = createTrajectoryFastStraight();
        double[][] trajectoryMediumAccelerate = createTrajectoryMediumAccelerate();
        double[][] trajectoryFastAccelerate = createTrajectoryFastAccelerate();
        double[][] trajectoryMediumBreak = createTrajectoryMediumBreak();
        double[][] trajectoryFastBreak = createTrajectoryFastBreak();
        
        Map<double[][], Integer> profileBase = new HashMap<double[][], Integer>();
        profileBase.put(trajectorySlowStraight, 600);
        profileBase.put(trajectoryMediumFastStraight, 800);
        profileBase.put(trajectoryFastStraight, 600);
        
        profileBase.put(trajectoryMediumAccelerate, 400);
        profileBase.put(trajectoryFastAccelerate, 400);
        profileBase.put(trajectoryMediumBreak, 600);
        profileBase.put(trajectoryFastBreak, 100);
    }

    private static double[][] createTrajectoryFastStraight()
    {
        return createStraightTrajectory(120.0);
    }

    private static double[][] createTrajectoryMediumFastStraight()
    {
        return createStraightTrajectory(60.0);
    }

    public static double[][] createTrajectorySlowStraight()
    {
        return createStraightTrajectory(15.0);
    }
    
    private static double[][] createTrajectoryMediumAccelerate()
    {
        return createStraightAccelerationTrajectory(10.0, 60.0);
    }

    private static double[][] createTrajectoryFastBreak()
    {
        return createStraightAccelerationTrajectory(120.0, 5.0);
    }

    private static double[][] createTrajectoryMediumBreak()
    {
        return createStraightAccelerationTrajectory(80.0, 20.0);
    }

    private static double[][] createTrajectoryFastAccelerate()
    {
        return createStraightAccelerationTrajectory(5.0, 120.0);
    }
    
    public static double[][] createStraightTrajectory(double velocityKmPHour)
    {
        double[][] result = new double[21][3];
        for(int idx = 0; idx < 21; idx++)
        {
            result[idx][0] = 5.0 * (double)idx;
            result[idx][1] = 0.0;
            result[idx][2] = UnitConverter.kilometersPerHourToMetersPerSecond(velocityKmPHour);
        }
        return result;
    }

    private static double[][] createStraightAccelerationTrajectory(double startVelocity, double finalVelocity)
    {
        double[][] trajectorySlowStraight = new double[21][3];
        List<Double> linspace = Linspace.linspace(startVelocity, finalVelocity, 21);
        for(int idx = 0; idx < 21; idx++)
        {
            trajectorySlowStraight[idx][0] = 5.0 * (double)idx;
            trajectorySlowStraight[idx][1] = 0.0;
            trajectorySlowStraight[idx][2] = UnitConverter.kilometersPerHourToMetersPerSecond(linspace.get(idx));
        }
        return trajectorySlowStraight;
    }
}
