package de.joachim.haensel.phd.scenario.navigation.test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.NullBehaviorActuatingSensing;

public class Positioner extends NullBehaviorActuatingSensing
{
    private Position2D _position;
    private Vector2D _orientation;

    public Positioner(Position2D position)
    {
        _position = position;
    }
    
    public Positioner(Position2D position, Position2D orientation)
    {
        this(position);
        _orientation = new Vector2D(0.0, 0.0, orientation.getX(), orientation.getY());
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

    @Override
    public Vector2D getOrientation()
    {
        return _orientation;
    }
}