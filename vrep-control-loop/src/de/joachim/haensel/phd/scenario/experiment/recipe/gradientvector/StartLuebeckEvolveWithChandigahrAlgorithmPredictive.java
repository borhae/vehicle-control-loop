package de.joachim.haensel.phd.scenario.experiment.recipe.gradientvector;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.loadClusteringK;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.joachim.haensel.phd.scenario.experiment.evaluation.RiskAnalysis;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.TrajectoryLoader;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.math.FromTo;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class StartLuebeckEvolveWithChandigahrAlgorithmPredictive
{
    public static void main(String[] args)
    {
        TrajectoryLoader trajectoryLoader = new TrajectoryLoader();
        Map<Integer, MongoTrajectory> dbTrajectories = trajectoryLoader.loadIndexedDataToMap("data_available_at_2020-01-18");
        Map<Trajectory3DSummaryStatistics, List<Integer>> clustering = loadClusteringK(200);
        Random evolveRnd = new MersenneTwister(1001);
        Random shuffleRnd = new MersenneTwister(1002);
        double riskUpperBound = Math.pow(10.0, -4.0);
        double maxTests = 10000.0;
        int maxAdditionalTests = 1;
//        runEvolve(dbTrajectories, clustering, randomGen, riskUpperBound, maxTests, "Luebeck", "Chandigarh");
        runEvolve(dbTrajectories, clustering, evolveRnd, shuffleRnd, riskUpperBound, maxTests, maxAdditionalTests, "Chandigarh", "Luebeck");
    }

    private static void runEvolve(Map<Integer, MongoTrajectory> dbTrajectories, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Random evolveRnd, Random shuffleRnd, double riskUpperBound, double maxTests, int maxAdditionalTests, String startCityName, String evolveIntoCityName)
    {
        SimulationBySampling sampler = new SimulationBySampling(evolveRnd, shuffleRnd, startCityName, evolveIntoCityName, clustering, dbTrajectories);
        System.out.println("Creating profile");
        sampler.setCyclesToSample(40000);
        sampler.setSamplesPerCycle(1000);
        ArrayList<FromTo> samplingProfile = 
                new ArrayList<FromTo>(Arrays.asList(
                        new FromTo(0.0, 0.0),
                        new FromTo(0.0, 0.5),
                        new FromTo(0.5, 0.5),
                        new FromTo(0.5, 1.0),
                        new FromTo(1.0, 1.0),
                        new FromTo(1.0, 1.0),
                        new FromTo(1.0, 1.0),
                        new FromTo(1.0, 1.0),
                        new FromTo(1.0, 1.0),
                        new FromTo(1.0, 1.0)
                )
        );
        sampler.setSamplingProfile(samplingProfile);
        System.out.println("done creating profile");
        sampler.initialize();
        
        CycleBasedRiskAnalysis cycleAnalysis = new CycleBasedRiskAnalysis(sampler);
        cycleAnalysis.setRiskUppeberBound(riskUpperBound);
        cycleAnalysis.setMaxAdditionalTests(maxAdditionalTests);
        cycleAnalysis.initialize();
        
        cycleAnalysis.initializeTestDistribution();
        cycleAnalysis.initializeUpperBoundStrategy();
        cycleAnalysis.initializeAdditionalTestsStrategy();
        cycleAnalysis.initializeAdditionalTestsUpperBoundStrategy();
        
        try
        {
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
            
            String allInfoFileName = String.format("./evolve%sTo%s_k%dAddm%smoreTestsAllInfosPred.csv", startCityName, evolveIntoCityName, clustering.keySet().size(), maxAdditionalTests);
            BufferedWriter allInfoWriter = Files.newBufferedWriter(Paths.get(allInfoFileName));
            String allInfoHeaderRow = String.format("diff_p batchCnt%1$s batchCnt%2$s nr_newTests_ub nr_newTests_add nr_newTests_add_ub nr_allTestsUb nr_allTestsAdd nr_allTestsAddUb risk_ub risk_add risk_add_ub risk_no_added_test risk_diff_ub risk_diff_add risk_diff_add_ub", startCityName, evolveIntoCityName);
            allInfoWriter.write(allInfoHeaderRow);
            allInfoWriter.newLine();

            while(sampler.hasMoreCycles())
            {
                sampler.sample();
                cycleAnalysis.setupNextCycle();
                
                cycleAnalysis.applyMaintainUpperBoundStrategy();
                cycleAnalysis.applyAddTestsConstantRateStrategy();
                cycleAnalysis.applyAddTestsConstantRateAndMaintainUpperBoundStrategy();
                
                cycleAnalysis.updateTestDistributions();
                
                //Compute risk gradient
                double deltaMaintainUpperBound = cycleAnalysis.deltaMaintainUpperBound(cycleAnalysis.getCurrentCycle() -1);
                double deltaAddConstantRate = cycleAnalysis.deltaAddConstantRate(cycleAnalysis.getCurrentCycle() -1);
                double deltaAddConstantRateMaintainUpperBound = cycleAnalysis.deltaAddConstantRateMaintainUpperBound(cycleAnalysis.getCurrentCycle() -1);

                String riskDiffUb = Double.toString(deltaMaintainUpperBound);
                String riskDiffAded = Double.toString(deltaAddConstantRate);
                String riskDiffAddedUb = Double.toString(deltaAddConstantRateMaintainUpperBound);


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

                allInfoWriter.write(String.format("%f %d %d %s %s %s %s %s %s %s %s %s %s %s %s %s", diff_p, batchCntStartCity, batchCntEvolveIntoCity, ubNewTests, addedNewTests, addedUbNewTests, ubAllTests, addedAllTests, addedUbAllTests, ubRisk, addedNewTestsRisk, addedUbTestsRisk, bareRiskDiff, riskDiffUb, riskDiffAded, riskDiffAddedUb));
                allInfoWriter.newLine();
            }
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
