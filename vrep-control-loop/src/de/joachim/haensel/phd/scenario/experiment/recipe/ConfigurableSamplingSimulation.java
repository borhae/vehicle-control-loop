package de.joachim.haensel.phd.scenario.experiment.recipe;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.loadClusteringK;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;

import de.joachim.haensel.phd.scenario.experiment.evaluation.RiskAnalysis;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.experiment.recipe.gradientvector.CycleBasedRiskAnalysis;
import de.joachim.haensel.phd.scenario.experiment.recipe.gradientvector.SimulationBySampling;
import de.joachim.haensel.phd.scenario.math.FromTo;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

// res\samplingconfigs
public class ConfigurableSamplingSimulation
{
    public static void main(String[] args)
    {
//        String configFileName = args[0];
        String configFileName = "./res/samplingconfigs/testsampling.cfg";
//        ObjectMapper mapper = new ObjectMapper();
//        try
//        {
//            SamplingExperimentConfiguration config = mapper.readValue(new File(configFileName), SamplingExperimentConfiguration.class);
//        } 
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
        Config configobj = ConfigFactory.parseFile(new File(configFileName), ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF));
        SamplingExperimentConfiguration config = ConfigBeanFactory.create(configobj, SamplingExperimentConfiguration.class);
        ConfigurableSamplingSimulation runner = new ConfigurableSamplingSimulation();
        runner.run(config);

    }

    private void run(SamplingExperimentConfiguration configuration)
    {
        int evolveRndSeed = configuration.getEvolveRndSeed();
        int pickSampleRnd = configuration.getPickSampleRndSeed();
        int maxAdditionalTests = configuration.getMaxAdditionalTests();
        double startWeigh = configuration.getStartWeight();
        double riskUpperBound = configuration.getRiskUpperBound();
        int clusteringK = configuration.getClusteringK();
        String dataSetName = configuration.getDataSetName();
        String startCityName = configuration.getStartCityName();
        String evolveIntoCityName = configuration.getEvolveIntoCityName();
        int cyclesToSample = configuration.getCyclesToSample();
        int samplesPerCycle = configuration.getSamplesPerCycle();
        List<FromTo> samplingProfile = configuration.getSamplingProfile();

        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        //Dataset contains 1266747 entries
        System.out.println("loading trajectories");
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap(dataSetName);
        System.out.println("loading clustering");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(clusteringK);
        
        System.out.println("initialize sampling experiment");
        Random evolveRnd = new MersenneTwister(evolveRndSeed);
        Random shuffleRnd = new MersenneTwister(pickSampleRnd);
        SimulationBySampling sampler = new SimulationBySampling(evolveRnd, shuffleRnd, startCityName, evolveIntoCityName, clustering, dbTrajectories, startWeigh);
        sampler.setCyclesToSample(cyclesToSample);
        sampler.setSamplesPerCycle(samplesPerCycle);
        System.out.println("creating profile");
        sampler.setSamplingProfile(samplingProfile);
        System.out.println("done creating profile");
        sampler.initialize();
        
        CycleBasedRiskAnalysis cycleAnalysis = new CycleBasedRiskAnalysis(sampler);
        cycleAnalysis.setRiskUppeberBound(riskUpperBound);
        cycleAnalysis.setMaxAdditionalTests(maxAdditionalTests);
        System.out.println("initializing risk analysis");
        cycleAnalysis.initialize();
        
        System.out.println("create first distribution of tests");
        cycleAnalysis.initializeTestDistribution();
        System.out.println("initializing strategies");
        System.out.println("1");
        cycleAnalysis.initializeUpperBoundStrategy();
        System.out.println("2");
        cycleAnalysis.initializeAdditionalTestsStrategy();
        System.out.println("3");
        cycleAnalysis.initializeAdditionalTestsUpperBoundStrategy();
        try
        {
            System.out.println("file stuff");
            String ubFileName = String.format("./evolve%sTo%s_k%dMaintainUPPred.csv", startCityName, evolveIntoCityName, clustering.keySet().size());
            BufferedWriter ubWriter = Files.newBufferedWriter(Paths.get(ubFileName));
            String ubHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests nr_allTests risk risk_no_added_test", startCityName, evolveIntoCityName);
            ubWriter.write(ubHeaderRow);
            ubWriter.newLine();

            String addedFileName = String.format("./evolve%sTo%s_k%dAddm%smoreTestsPred.csv", startCityName, evolveIntoCityName, clustering.keySet().size(), maxAdditionalTests);
            BufferedWriter addedWriter = Files.newBufferedWriter(Paths.get(addedFileName));
            String addedHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests nr_allTests risk risk_no_added_test", startCityName, evolveIntoCityName);
            addedWriter.write(addedHeaderRow);
            addedWriter.newLine();

            String addedUbFileName = String.format("./evolve%sTo%s_k%dAddm%smoreTestsMaintainUBPred.csv", startCityName, evolveIntoCityName, clustering.keySet().size(), maxAdditionalTests);
            BufferedWriter addedUbWriter = Files.newBufferedWriter(Paths.get(addedUbFileName));
            String addedUbHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests nr_allTests risk risk_no_added_test", startCityName, evolveIntoCityName);
            addedUbWriter.write(addedUbHeaderRow);
            addedUbWriter.newLine();
            
            String allInfoFileName = String.format("./evolve%sTo%s_k%dAddm%smoreTests%.2fscaledInit%dsamplePerCycle%dCyclesAllInfosPred.csv", startCityName, evolveIntoCityName, clustering.keySet().size(), maxAdditionalTests, configuration.getStartWeight(), configuration.getSamplesPerCycle(), configuration.getCyclesToSample());
            BufferedWriter allInfoWriter = Files.newBufferedWriter(Paths.get(allInfoFileName));
            String allInfoHeaderRow = String.format("cycle diff_p batchCnt%1$s batchCnt%2$s nr_newTests_ub nr_newTests_add nr_newTests_add_ub nr_allTestsUb nr_allTestsAdd nr_allTestsAddUb risk_ub risk_add risk_add_ub risk_no_added_test risk_diff_ub risk_diff_add risk_diff_add_ub risk_no_add_test", startCityName, evolveIntoCityName);
            allInfoWriter.write(allInfoHeaderRow);
            allInfoWriter.newLine();

            System.out.println("sampling loop starts");
            long startSampling = System.currentTimeMillis();
            while(sampler.hasMoreCycles())
            {
                sampler.sampleWithReplacement();
                cycleAnalysis.setupNextCycle();
                
                cycleAnalysis.applyMaintainUpperBoundStrategy();
                cycleAnalysis.applyAddTestsConstantRateStrategy();
                cycleAnalysis.applyAddTestsConstantRateAndMaintainUpperBoundStrategy();
                
                cycleAnalysis.updateTestDistributions();
                
                //Compute risk gradient
                double deltaMaintainUpperBound = cycleAnalysis.deltaMaintainUpperBound(cycleAnalysis.getCurrentCycle() -1);
                double deltaAddConstantRate = cycleAnalysis.deltaAddConstantRate(cycleAnalysis.getCurrentCycle() -1);
                double deltaAddConstantRateMaintainUpperBound = cycleAnalysis.deltaAddConstantRateMaintainUpperBound(cycleAnalysis.getCurrentCycle() -1);
                double deltaNoAdditionalTests = cycleAnalysis.deltaNoAdditionalTests(cycleAnalysis.getCurrentCycle() - 1);

                String riskDiffUb = Double.toString(deltaMaintainUpperBound);
                String riskDiffAded = Double.toString(deltaAddConstantRate);
                String riskDiffAddedUb = Double.toString(deltaAddConstantRateMaintainUpperBound);
                String riskNoAddedTests = Double.toString(deltaNoAdditionalTests);


                String ubNewTests = Double.toString(cycleAnalysis.ubNewTests());
                String ubAllTests = Double.toString(cycleAnalysis.ubAllTests());
                String ubRisk = Double.toString(cycleAnalysis.ubRisk());
                
                String addedNewTests = Double.toString(cycleAnalysis.addedNewTests());
                String addedAllTests = Double.toString(cycleAnalysis.adddedAllTests());
                String addedNewTestsRisk = Double.toString(cycleAnalysis.addedNewTestsRisk());

                String addedUbNewTests = Double.toString(cycleAnalysis.addedUbNewTests());
                String addedUbAllTests = Double.toString(cycleAnalysis.addedUbAllTests());
                String addedUbTestsRisk = Double.toString(cycleAnalysis.addedUbTestsRisk());
                
                double diff_p = RiskAnalysis.deltaProfile(cycleAnalysis.getCurrentProfile_p(), cycleAnalysis.getStartCity_p());

                int batchCntStartCity = sampler.getBatchCntStartCity();
                int batchCntEvolveIntoCity = sampler.getBatchCntEvolveIntoCity();
                String bareRiskDiff = Double.toString(cycleAnalysis.getBareRiskDiff());
                ubWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, ubNewTests, ubAllTests, ubRisk, bareRiskDiff));
                ubWriter.newLine();
                
                addedWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, addedNewTests, addedAllTests, addedNewTestsRisk, bareRiskDiff));
                addedWriter.newLine();

                addedUbWriter.write(String.format("%f %d %d %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, addedUbNewTests, addedUbAllTests, addedUbTestsRisk, bareRiskDiff));
                addedUbWriter.newLine();

                allInfoWriter.write(String.format("%d %f %d %d %s %s %s %s %s %s %s %s %s %s %s %s %s %s", sampler.getCycle(), diff_p, batchCntStartCity, batchCntEvolveIntoCity, ubNewTests, addedNewTests, addedUbNewTests, ubAllTests, addedAllTests, addedUbAllTests, ubRisk, addedNewTestsRisk, addedUbTestsRisk, bareRiskDiff, riskDiffUb, riskDiffAded, riskDiffAddedUb, riskNoAddedTests));
                allInfoWriter.newLine();
            }
            long finishSampling = System.currentTimeMillis();
            System.out.format("done sampling, took: %d seconds\n", (finishSampling - startSampling)/1000);
            ubWriter.flush();
            ubWriter.close();
            addedWriter.flush();
            addedWriter.close();
            addedUbWriter.flush();
            addedUbWriter.close();
            allInfoWriter.flush();
            allInfoWriter.close();
        }
        catch (IOException  exc) 
        {
            exc.printStackTrace();
        }
    }
}
