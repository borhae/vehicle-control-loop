package de.joachim.haensel.phd.scenario.experiment.runner;

import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener.IIDCreator;

public class RouteIDCreator implements IIDCreator
{
    private Integer _counter = Integer.valueOf(0);

    public RouteIDCreator(int routeStartIdx)
    {
        _counter = Integer.valueOf(routeStartIdx);
    }
    
    public synchronized String getNextStringID()
    {
        Integer next = Integer.valueOf(_counter.intValue() + 1);
        _counter = next;
        return _counter.toString();
    }
}