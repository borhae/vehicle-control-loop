package de.joachim.haensel.phd.scenario.navigation.test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class NumberedRoute
{
    private Position2D _start;
    private Position2D _end;
    private int _routeNumber;

    public NumberedRoute(Position2D start, Position2D end, int routeNumber)
    {
        _start = start;
        _end = end;
        _routeNumber = routeNumber;
    }

    public Position2D getStart()
    {
        return _start;
    }

    public Position2D getEnd()
    {
        return _end;
    }

    public int getRouteNumber()
    {
        return _routeNumber;
    }
}
