package de.joachim.haensel.phd.scenario.experiment.recipe.gradientvector;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.retreiveProfileFrom;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.retreiveProfileFromIndexed;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;
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
    
    private List<List<Integer>> _t_i_maintainUpperBound;
    private List<List<Integer>> _t_i_minimizeWithAdditionalTests;
    private List<List<Integer>> _t_i_minimizeWithAdditionalTestsMaintainUpperBound;
    
    private List<Integer> _newTestsMaintainUpperBound;
    private List<Integer> _newTestsMinimizeWithAdditional;
    private List<Integer> _newTestsMinimizeWithAdditionalAndMaintain;

    private List<List<Double>> _currentProfile_p;
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
        _startCity_p = retreiveProfileFrom(_sampler.getStartCityClustering());
        _currentProfile_p = new ArrayList<List<Double>>();
    }

    public void updateTestDistributions()
    {
        updateUpperBoundStrategyDistribution();
        updateAdditionalTestsStrageyDistribution();
        updateAdditionalTestsMaintainUpperBoundStrategyDistribution();
    }

    public double deltaMaintainUpperBound(int cycleIdx)
    {
        return buildDelta(cycleIdx, _t_i_maintainUpperBound);
    }

    public double deltaAddConstantRate(int cycleIdx)
    {
        return buildDelta(cycleIdx, _t_i_minimizeWithAdditionalTests);
    }

    public double deltaAddConstantRateMaintainUpperBound(int cycleIdx)
    {
        return buildDelta(cycleIdx, _t_i_minimizeWithAdditionalTestsMaintainUpperBound);
    }

    private double buildDelta(int cycleIdx, List<List<Integer>> t_i)
    {
        // as described in paper the cycle index is called c
        int c = _currentCycle;
        if(cycleIdx < 0 || c - cycleIdx <= 0)
        {
            return 0.0;
        }
        else
        {
            int size = _currentProfile_p.get(c).size();
            IntToDoubleFunction mapper = 
                    idx -> 
                {
                    return (_currentProfile_p.get(c).get(idx) - _currentProfile_p.get(c - 1).get(idx)) / (2 + (double)t_i.get(c - 1).get(idx));
                };
            return IntStream.range(0, size).mapToDouble(mapper).sum();
        }
    }
    
    public int getCurrentCycle()
    {
        System.out.println("Cycle: " + _currentCycle);
        return _currentCycle;
    }

    public void initializeTestDistribution()
    {
        System.out.println("_initial_tis = RiskAnalysisAlgorithmically.compute_t_iGivenR(_startCity_p, _riskUpperBound);");
        _initial_tis = RiskAnalysisAlgorithmically.compute_t_iGivenR(_startCity_p, _riskUpperBound);
    }

    public void initializeUpperBoundStrategy()
    {
        ArrayList<Integer> firstEntry = new ArrayList<Integer>(_initial_tis);
        List<List<Integer>> t_i_maintainUpperBound = new ArrayList<List<Integer>>();
        t_i_maintainUpperBound.add(firstEntry);
        _t_i_maintainUpperBound = t_i_maintainUpperBound;
    }

    public void initializeAdditionalTestsStrategy()
    {
        ArrayList<Integer> firstEntry = new ArrayList<Integer>(_initial_tis);
        List<List<Integer>> t_i_minimizeWithAdditionalTests = new ArrayList<List<Integer>>();
        t_i_minimizeWithAdditionalTests.add(firstEntry);
        _t_i_minimizeWithAdditionalTests = t_i_minimizeWithAdditionalTests;
    }

    public void initializeAdditionalTestsUpperBoundStrategy()
    {
        ArrayList<Integer> firstEntry = new ArrayList<Integer>(_initial_tis);
        List<List<Integer>> t_i_minimizeWithAdditionalTestsMaintainUpperBound = new ArrayList<List<Integer>>();
        t_i_minimizeWithAdditionalTestsMaintainUpperBound.add(firstEntry);
        _t_i_minimizeWithAdditionalTestsMaintainUpperBound = t_i_minimizeWithAdditionalTestsMaintainUpperBound;
    }

    public void setupNextCycle()
    {
        _currentCycle = _sampler.getCycle();
        _currentProfile_p.add(retreiveProfileFromIndexed(_sampler.getClusterCounts()));
        _bareRiskDiff = RiskAnalysisAlgorithmically.computeR(_initial_tis, _currentProfile_p.get(_currentCycle));
    }

    private void updateUpperBoundStrategyDistribution()
    {
        _t_i_maintainUpperBound.add(merge(_newTestsMaintainUpperBound, _t_i_maintainUpperBound.get(_currentCycle)));
        
        _ubNewTests = _newTestsMaintainUpperBound.stream().mapToDouble(Double::valueOf).sum();
        _ubAllTests = _t_i_maintainUpperBound.get(_currentCycle + 1).stream().mapToDouble(Double::valueOf).sum();
        _ubRisk = RiskAnalysisAlgorithmically.computeR(_t_i_maintainUpperBound.get(_currentCycle + 1), _currentProfile_p.get(_currentCycle));
    }

    private void updateAdditionalTestsStrageyDistribution()
    {
        _t_i_minimizeWithAdditionalTests.add(merge(_newTestsMinimizeWithAdditional, _t_i_minimizeWithAdditionalTests.get(_currentCycle)));
        
        _addedNewTests = _newTestsMinimizeWithAdditional.stream().mapToDouble(Double::valueOf).sum();
        _addedAllTests = _t_i_minimizeWithAdditionalTests.get(_currentCycle + 1).stream().mapToDouble(Double::valueOf).sum();
        _addedNewTestsRisk = RiskAnalysisAlgorithmically.computeR(_t_i_minimizeWithAdditionalTests.get(_currentCycle + 1), _currentProfile_p.get(_currentCycle));
    }

    private void updateAdditionalTestsMaintainUpperBoundStrategyDistribution()
    {
        _t_i_minimizeWithAdditionalTestsMaintainUpperBound.add(merge(_newTestsMinimizeWithAdditionalAndMaintain, _t_i_minimizeWithAdditionalTestsMaintainUpperBound.get(_currentCycle)));
        _addedUbNewTests = _newTestsMinimizeWithAdditionalAndMaintain.stream().mapToDouble(Double::valueOf).sum();
        _addedUbAllTests = _t_i_minimizeWithAdditionalTestsMaintainUpperBound.get(_currentCycle + 1).stream().mapToDouble(Double::valueOf).sum();
        _addedUbTestsRisk = RiskAnalysisAlgorithmically.computeR(_t_i_minimizeWithAdditionalTestsMaintainUpperBound.get(_currentCycle + 1), _currentProfile_p.get(_currentCycle));
    }

    /**
     * Strategy 1: add so many tests that we can keep the upper bound
     */
    public void applyMaintainUpperBoundStrategy()
    {
        List<Integer> t_is_old = _t_i_maintainUpperBound.get(_currentCycle);
        List<Double> currentProfile = _currentProfile_p.get(_currentCycle);
        if(t_is_old == null)
        {
            _newTestsMaintainUpperBound = RiskAnalysisAlgorithmically.compute_t_iGivenR(currentProfile, _riskUpperBound);
        }
        else
        {
            double currentRisk = RiskAnalysisAlgorithmically.computeR(t_is_old, currentProfile);
            if(currentRisk <= _riskUpperBound)
            {
                _newTestsMaintainUpperBound = IntStream.range(0, _currentProfile_p.get(_currentCycle).size()).map(idx -> 0).boxed().collect(Collectors.toList());
            }
            else
            {
                _newTestsMaintainUpperBound = RiskAnalysisAlgorithmically.t_iMinimizeTGivenRisk_p_iAndOldt_iDiffOnly(currentProfile, t_is_old, _riskUpperBound);
            }
        }
    }

    /**
     * Strategy 2: add a fixed amount of tests
     */
    public void applyAddTestsConstantRateStrategy()
    {
        _newTestsMinimizeWithAdditional = 
                RiskAnalysisAlgorithmically.t_iMinimizeRiskGivenT_p_iAndOldt_iDiffOnly(_currentProfile_p.get(_currentCycle), _t_i_minimizeWithAdditionalTests.get(_currentCycle), _maxAdditionalTests);
    }    

    /**
     * Strategy 3: add a fixed amount of tests per cycle and add more if we can't keep the upper bound
     */
    public void applyAddTestsConstantRateAndMaintainUpperBoundStrategy()
    {
        List<Integer> t_is = _t_i_minimizeWithAdditionalTestsMaintainUpperBound.get(_currentCycle);
        List<Double> currentProfile = _currentProfile_p.get(_currentCycle);
        List<Integer> additionalTestsGivenT = RiskAnalysisAlgorithmically.t_iMinimizeRiskGivenT_p_iAndOldt_iDiffOnly(currentProfile, t_is, _maxAdditionalTests);
        List<Integer> result = merge(additionalTestsGivenT, t_is);
        List<Integer> additionalTestsGivenUB = RiskAnalysisAlgorithmically.t_iMinimizeTGivenRisk_p_iAndOldt_iDiffOnly(currentProfile, result, _riskUpperBound);
        result = merge(additionalTestsGivenT, additionalTestsGivenUB);
        _newTestsMinimizeWithAdditionalAndMaintain = result;
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
        return _currentProfile_p.get(_currentCycle);
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
