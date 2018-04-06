package de.joachim.haensel.phd.scenario.navigation.test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.vehicle.IActuatingSensing;

public class Positioner implements IActuatingSensing
{
    private Position2D _position;

    public Positioner(Position2D position)
    {
        _position = position;
    }

    @Override
    public void setPosition(float posX, float posY, float posZ)
    {
    }

    @Override
    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma)
    {
    }

    @Override
    public double getVehicleLength()
    {
        return 0;
    }

    @Override
    public Position2D getRearWheelCenterPosition()
    {
        return null;
    }

    @Override
    public Position2D getPosition()
    {
        return _position;
    }

    @Override
    public Vector2D getOrientation()
    {
        return null;
    }

    @Override
    public Position2D getFrontWheelCenterPosition()
    {
        return null;
    }

    @Override
    public void drive(float targetWheelRotation, float targetSteeringAngle)
    {
    }

    @Override
    public void computeAndLockSensorData()
    {
    }
}