package de.joachim.haensel.phd.scenario.experiment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.experiment.runner.ExperimentRunner;
import de.joachim.haensel.phd.scenario.experiment.runner.ExperimentRunnerAdaptingController;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class AdaptiveControllerMinimalExperimentTest
{
    @Test
    public void testMinimalRundManyTargets()
    {
        System.out.println("running");
        try
        {
            ExperimentRunnerAdaptingController runner = new ExperimentRunnerAdaptingController(false);
            runner.initialize();
            try
            {
                List<String> pointsAsString = Files.readAllLines(new File(ExperimentRunner.RES_ROADNETWORKS_DIRECTORY + "NeumarkRealWorldNoTrainsPoints.txt").toPath());
                List<Position2D> positions = pointsAsString.stream().map(string -> new Position2D(string)).collect(Collectors.toList());
                runner.run("luebeck_183_max_scattered_targets", 15.0, 120.0, 4.0, 4.3, 1.0, positions, "neumarkRealWorldNoTrains.net.xml", "blue");
                runner.tearDown();
            }
            catch (IOException exc)
            {
                exc.printStackTrace();
            }
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }
}
