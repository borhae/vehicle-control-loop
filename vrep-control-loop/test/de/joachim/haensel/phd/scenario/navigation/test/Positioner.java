package de.joachim.haensel.phd.scenario.navigation.test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.NullBehaviorActuatingSensing;

public class Positioner extends NullBehaviorActuatingSensing
{
    private Position2D _position;

    public Positioner(Position2D position)
    {
        _position = position;
    }

    @Override
    public Position2D getPosition()
    {
        return _position;
    }
    
    @Override
    public Position2D getNonDynamicPosition()
    {
        return _position;
    }
}