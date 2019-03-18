package de.joachim.haensel.phd.scenario.experiment.evaluation.test;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.scenario.experiment.evaluation.TestSumOptimizer;

public class TestMinimizer
{
    @Test
    public void testMinimizer()
    {
        TestSumOptimizer optimizer = new TestSumOptimizer();
        List<Double> probabilities = Arrays.asList(0.1, 0.2, 0.7);
        double upperBound = 0.001;
        int testResources = 1000;
        
        List<Integer> minimizeAmountOfTests = optimizer.minimizeAmountOfTests(probabilities, upperBound);
        int sum = minimizeAmountOfTests.stream().mapToInt(v -> v).sum();
        System.out.println("tada! " + sum);
        List<Integer> minimizeUpperBound = optimizer.minimizeUpperBound(probabilities, testResources);
    }
}
