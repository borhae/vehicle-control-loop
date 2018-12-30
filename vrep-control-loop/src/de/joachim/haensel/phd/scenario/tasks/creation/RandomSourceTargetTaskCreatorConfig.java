package de.joachim.haensel.phd.scenario.tasks.creation;

import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.tasks.DriveAtoBTask;

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
    public DriveAtoBTask getNext()
    {
        if(_range == null)
        {
            return null;
        }
        else
        {
            double xS = _randomGen.nextDouble() * _range.distX() + _range.minX();
            double yS = _randomGen.nextDouble() * _range.distY() + _range.minY();
            double xT = _randomGen.nextDouble() * _range.distX() + _range.minX();
            double yT = _randomGen.nextDouble() * _range.distY() + _range.minY();
            int timeoutSec = ITaskCreatorConfig.estimateTimeout(xS, yS, xT, yT);
            // TODO add vehicle creation stuff and then add that to the drive task
//            return new DriveAtoBTask(xS, yS, xT, yT, timeoutSec);
            return null;
        }
    }

    public void setXYRange(XYMinMax range)
    {
        _range = range;
    }

    @Override
    public boolean hasNext()
    {
        if(_range == null)
        {
            return false;
        }
        org.junit.Assert.fail();
        return false;
    }

    @Override
    public void init()
    {
        org.junit.Assert.fail();
    }
}
