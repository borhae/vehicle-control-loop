package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

public class Route
{
    private List<Trajectory> _route;
    private int _currentIdx;

    public void createRoute(List<Trajectory> trajectory)
    {
        _route = trajectory;
        _currentIdx = 0;
    }

    public List<Trajectory> getSegments(int requestSize)
    {
        int lowerIdx = _currentIdx;
        if(_route.size() < lowerIdx + requestSize)
        {
            return null;
        }
        int higherIdx = Math.min(lowerIdx + requestSize, _route.size());
        _currentIdx += requestSize;
        return _route.subList(lowerIdx, higherIdx);
    }

    public Trajectory peek()
    {
        return _route.get(0);
    }

    public int getSize()
    {
        return _route.size();
    }
}
