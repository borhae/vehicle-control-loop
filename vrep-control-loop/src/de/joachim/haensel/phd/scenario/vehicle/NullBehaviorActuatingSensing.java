package de.joachim.haensel.phd.scenario.vehicle;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

/**
 * Does nothing. For extending when not all methods are necessary (TODO split this interface)
 * @author dummy
 *
 */
public class NullBehaviorActuatingSensing implements IActuatingSensing
{
    @Override
    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma)
    {
    }

    @Override
    public void setPosition(float posX, float posY, float posZ)
    {
    }

    @Override
    public void computeAndLockSensorData()
    {
    }

    @Override
    public long getTimeStamp()
    {
        return 0;
    }

    @Override
    public void blowTire(boolean[] tiresToBlow, float tireScale)
    {
    }

    @Override
    public Vector2D getOrientation()
    {
        return null;
    }

    @Override
    public Position2D getPosition()
    {
        return null;
    }

    @Override
    public Position2D getNonDynamicPosition()
    {
        return null;
    }

    @Override
    public Position2D getRearWheelCenterPosition()
    {
        return null;
    }

    @Override
    public Position2D getFrontWheelCenterPosition()
    {
        return null;
    }

    @Override
    public double getVehicleLength()
    {
        return 0;
    }

    @Override
    public double getWheelDiameter()
    {
        return 0;
    }

    @Override
    public double[] getVehicleVelocity()
    {
        return null;
    }

    @Override
    public void drive(float targetWheelRotation, float targetSteeringAngle)
    {
    }

    @Override
    public void initialize()
    {
    }

    @Override
    public Vector2D getLockedOrientation()
    {
        return null;
    }
}
