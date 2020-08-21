package de.joachim.haensel.phd.scenario.experiment.recipe;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.loadClusteringK;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class ConfigurableSamplingSimulation
{
    public static void main(String[] args)
    {
        Config configobj = ConfigFactory.parseFile(new File(args[0]), ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF));
        SamplingExperimentConfiguration configuration = ConfigBeanFactory.create(configobj, SamplingExperimentConfiguration.class);

        int evolveRndSeed = configuration.getEvolveRndSeed();//1001;
        int pickSampleRnd = configuration.getPickSampleRndSeed();//1002;
        double maxTests = 10000.0;
        int maxAdditionalTests = 6;
        double startWeigh = 100.0;
        double riskUpperBound = Math.pow(10.0, -4.0);

        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        //Dataset contains 1266747 entries
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(200);

        Random evolveRnd = new MersenneTwister(evolveRndSeed);
        Random shuffleRnd = new MersenneTwister(pickSampleRnd);
//        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, "Luebeck", "Chandigarh");
    }
}
