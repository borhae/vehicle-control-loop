package de.joachim.haensel.vehicle;

import de.joachim.haensel.sumo2vrep.Position2D;

public interface IActuatingSensing
{
    public Position2D getPosition();

    public void drive(float targetWheelRotation, float targetSteeringAngle);

    public Position2D getRearWheelCenterPosition();
}
