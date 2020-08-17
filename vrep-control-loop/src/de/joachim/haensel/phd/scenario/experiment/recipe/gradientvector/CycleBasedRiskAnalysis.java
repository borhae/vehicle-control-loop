package de.joachim.haensel.phd.scenario.experiment.recipe.gradientvector;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.retreiveProfileFrom;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.retreiveProfileFromIndexed;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.RiskAnalysisAlgorithmically;

/**
 * This class provides a calculation method for the difference in risk to previous cycles in computations.
 * Test and usage distributions are stored to build these differences. 
 * @author Simulator
 *
 */
public class CycleBasedRiskAnalysis
{
    private int _currentCycle;
    private List<Double> _startCity_p;
    private SimulationBySampling _sampler;
    private List<Integer> _initial_tis;
    private List<Integer> _t_i_maintainUpperBound;
    private List<Integer> _t_i_minimizeWithAdditionalTests;
    private List<Integer> _t_i_minimizeWithAdditionalTestsMaintainUpperBound;
    private List<Double> _previousProfile_p;
    private List<Double> _currentProfile_p;
    private double _bareRiskDiff;
    private double _riskUpperBound;
    private double _ubNewTests;
    private double _ubAllTests;
    private double _ubRisk;
    private int _maxAdditionalTests;
    private double _addedNewTests;
    private double _addedAllTests;
    private double _addedNewTestsRisk;
    private double _addedUbNewTests;
    private double _addedUbAllTests;
    private double _addedUbTestsRisk;
    
    public CycleBasedRiskAnalysis(SimulationBySampling sampler)
    {
        _sampler = sampler;
        _currentCycle = sampler.getCycle();
    }
    
    public void initialize()
    {
        System.out.println("_startCity_p = retreiveProfileFrom(_sampler.getStartCityClustering());");
        _startCity_p = retreiveProfileFrom(_sampler.getStartCityClustering());
        System.out.println("_currentProfile_p = retreiveProfileFromIndexed(_sampler.getClusterCounts());");
        _currentProfile_p = retreiveProfileFromIndexed(_sampler.getClusterCounts());
        System.out.println("_previousProfile_p = retreiveProfileFromIndexed(_sampler.getClusterCounts());");
        _previousProfile_p = retreiveProfileFromIndexed(_sampler.getClusterCounts());
    }

    public void finishCycle()
    {
        System.out.println("_previousProfile_p = _currentProfile_p;");
        _previousProfile_p = _currentProfile_p;
    }

    public double delta(int cycleIdx)
    {
        //TODO implement me
        return 0.0;
    }

    public int getCurrentCycle()
    {
        return _currentCycle;
    }

    public void initializeTestDistribution()
    {
        System.out.println("_initial_tis = RiskAnalysisAlgorithmically.compute_t_iGivenR(_startCity_p, _riskUpperBound);");
        _initial_tis = RiskAnalysisAlgorithmically.compute_t_iGivenR(_startCity_p, _riskUpperBound);
    }

    public void initializeUpperBoundStrategy()
    {
        System.out.println("_t_i_maintainUpperBound = new ArrayList<Integer>(_initial_tis);");
        _t_i_maintainUpperBound = new ArrayList<Integer>(_initial_tis);
    }

    public void initializeAdditionalTestsStrategy()
    {
        System.out.println("_t_i_minimizeWithAdditionalTests = new ArrayList<Integer>(_initial_tis);");
        _t_i_minimizeWithAdditionalTests = new ArrayList<Integer>(_initial_tis);
    }

    public void initializeAdditionalTestsUpperBoundStrategy()
    {
        System.out.println("_t_i_minimizeWithAdditionalTestsMaintainUpperBound = new ArrayList<Integer>(_initial_tis);");
        _t_i_minimizeWithAdditionalTestsMaintainUpperBound = new ArrayList<Integer>(_initial_tis);
    }

    public void setupNextCycle()
    {
        _currentCycle = _sampler.getCycle();
        _currentProfile_p = retreiveProfileFromIndexed(_sampler.getClusterCounts());
        _bareRiskDiff = RiskAnalysisAlgorithmically.computeR(_initial_tis, _currentProfile_p);
    }

    /**
     * Strategy 1: add so many tests that we can keep the upper bound
     */
    public void applyMaintainUpperBoundStrategy()
    {
        List<Integer> newTestsMaintainUpperBound = 
                maintainUpperBound(_t_i_maintainUpperBound);
        _t_i_maintainUpperBound = merge(newTestsMaintainUpperBound, _t_i_maintainUpperBound);
        _ubNewTests = newTestsMaintainUpperBound.stream().mapToDouble(Double::valueOf).sum();
        _ubAllTests = _t_i_maintainUpperBound.stream().mapToDouble(Double::valueOf).sum();
        _ubRisk = RiskAnalysisAlgorithmically.computeR(_t_i_maintainUpperBound, _currentProfile_p);
    }

    /**
     * Strategy 2: add a fixed amount of tests
     */
    public void applyAddTestsConstantRateStrategy()
    {
        List<Integer> newTestsMinimizeWithAdditional = 
                minimizeWithAdditionalTests(_t_i_minimizeWithAdditionalTests, _currentProfile_p, _previousProfile_p, _maxAdditionalTests);
        _t_i_minimizeWithAdditionalTests = merge(newTestsMinimizeWithAdditional, _t_i_minimizeWithAdditionalTests);
        _addedNewTests = newTestsMinimizeWithAdditional.stream().mapToDouble(Double::valueOf).sum();
        _addedAllTests = _t_i_minimizeWithAdditionalTests.stream().mapToDouble(Double::valueOf).sum();
        _addedNewTestsRisk = RiskAnalysisAlgorithmically.computeR(_t_i_minimizeWithAdditionalTests, _currentProfile_p);
    }    

    /**
     * Strategy 3: add a fixed amount of tests per cycle and add more if we can't keep the upper bound
     */
    public void applyAddTestsConstantRateAndMaintainUpperBoundStrategy()
    {
        List<Integer> newTestsMinimizeWithAdditionalAndMaintain = 
                minimizeWithAdditionalTestsAndMaintainUpperBound(_t_i_minimizeWithAdditionalTestsMaintainUpperBound, _currentProfile_p, _previousProfile_p, _riskUpperBound, _maxAdditionalTests);
        _t_i_minimizeWithAdditionalTestsMaintainUpperBound = merge(newTestsMinimizeWithAdditionalAndMaintain, _t_i_minimizeWithAdditionalTestsMaintainUpperBound);
        _addedUbNewTests = newTestsMinimizeWithAdditionalAndMaintain.stream().mapToDouble(Double::valueOf).sum();
        _addedUbAllTests = _t_i_minimizeWithAdditionalTestsMaintainUpperBound.stream().mapToDouble(Double::valueOf).sum();
        _addedUbTestsRisk = RiskAnalysisAlgorithmically.computeR(_t_i_minimizeWithAdditionalTestsMaintainUpperBound, _currentProfile_p);
    }

    public double ubNewTests()
    {
        return _ubNewTests;
    }

    public double ubAllTests()
    {
        return _ubAllTests;
    }

    public double ubRisk()
    {
        return _ubRisk;
    }

    public void setRiskUppeberBound(double riskUpperBound)
    {
        _riskUpperBound = riskUpperBound;
    }
    
    public void setMaxAdditionalTests(int maxAdditionalTests)
    {
        _maxAdditionalTests = maxAdditionalTests;
    }
    
    private List<Integer> maintainUpperBound(List<Integer> t_is_old)
    {
        if(t_is_old == null)
        {
            return RiskAnalysisAlgorithmically.compute_t_iGivenR(_currentProfile_p, _riskUpperBound);
        }
        else
        {
            double currentRisk = RiskAnalysisAlgorithmically.computeR(t_is_old, _currentProfile_p);
            if(currentRisk <= _riskUpperBound)
            {
                return IntStream.range(0, _currentProfile_p.size()).map(idx -> 0).boxed().collect(Collectors.toList());
            }
            else
            {
                return RiskAnalysisAlgorithmically.t_iMinimizeTGivenRisk_p_iAndOldt_iDiffOnly(_currentProfile_p, t_is_old, _riskUpperBound);
            }
        }
    }
    
    private static List<Integer> minimizeWithAdditionalTests(List<Integer> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, int maxAdditionalTests)
    {
        return RiskAnalysisAlgorithmically.t_iMinimizeRiskGivenT_p_iAndOldt_iDiffOnly(currentProfile_p, t_is, maxAdditionalTests);
    }
    
    private static List<Integer> minimizeWithAdditionalTestsAndMaintainUpperBound(List<Integer> t_is, List<Double> currentProfile_p, List<Double> previousProfile_p, double riskUpperBound, int maxAdditionalTests)
    {
        List<Integer> additionalTestsGivenT = RiskAnalysisAlgorithmically.t_iMinimizeRiskGivenT_p_iAndOldt_iDiffOnly(currentProfile_p, t_is, maxAdditionalTests);
        List<Integer> result = merge(additionalTestsGivenT, t_is);
        List<Integer> additionalTestsGivenUB = RiskAnalysisAlgorithmically.t_iMinimizeTGivenRisk_p_iAndOldt_iDiffOnly(currentProfile_p, result, riskUpperBound);
        result = merge(additionalTestsGivenT, additionalTestsGivenUB);
        return result;
    }

    private static List<Integer> merge(List<Integer> additionalTests, List<Integer> currentTests)
    {
        if(currentTests == null)
        {
            return new ArrayList<Integer>(additionalTests);
        }
        else
        {
            return IntStream.range(0, currentTests.size()).map(idx -> currentTests.get(idx) + additionalTests.get(idx)).boxed().collect(Collectors.toList());
        }
    }

    public double addedNewTests()
    {
        return _addedNewTests;
    }

    public double adddedAllTests()
    {
        return _addedAllTests;
    }

    public double addedNewTestsRisk()
    {
        return _addedNewTestsRisk;
    }
    
    public double addedUbNewTests()
    {
        return _addedUbNewTests;
    }

    public double addedUbAllTests()
    {
        return _addedUbAllTests;
    }

    public double addedUbTestsRisk()
    {
        return _addedUbTestsRisk;
    }

    public List<Double> getCurrentProfile_p()
    {
        return _currentProfile_p;
    }

    public List<Double> getStartCity_p()
    {
        return _startCity_p;
    }

    public double getBareRiskDiff()
    {
        return _bareRiskDiff;
    }
}
