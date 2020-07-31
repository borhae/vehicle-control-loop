package de.joachim.haensel.phd.scenario.experiment.recipe.gradientvector;

import java.util.List;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;

public class GradientInRisk
{
    public static void main(String[] args)
    {
        TrajectoryLoader loader = new TrajectoryLoader();
        List<double[][]> data = loader.loadData(true, false, 0, 100);
        
    }
}
