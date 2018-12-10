package de.joachim.haensel.phd.scenario.tasks.creation;

import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class RandomSourceTargetTaskCreatorConfig implements ITaskCreatorConfig
{
    private int _numberOfTasks;
    private XYMinMax _range;
    private MersenneTwister _randomGen;

    public RandomSourceTargetTaskCreatorConfig(int numberOfTasks)
    {
        _numberOfTasks = numberOfTasks;
        _randomGen = new MersenneTwister(System.currentTimeMillis());
    }

    @Override
    public int getNumOfTasks()
    {
        return _numberOfTasks;
    }

    @Override
    public Task getNext()
    {
        if(_range == null)
        {
            return new Task();
        }
        else
        {
            double xS = _randomGen.nextDouble() * _range.distX() + _range.minX();
            double yS = _randomGen.nextDouble() * _range.distY() + _range.minY();
            double xT = _randomGen.nextDouble() * _range.distX() + _range.minX();
            double yT = _randomGen.nextDouble() * _range.distY() + _range.minY();
            return new Task(xS, yS, xT, yT);
        }
    }

    public void setXYRange(XYMinMax range)
    {
        _range = range;
    }
}
