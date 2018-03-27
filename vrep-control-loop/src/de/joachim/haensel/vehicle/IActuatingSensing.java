package de.joachim.haensel.vehicle;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public interface IActuatingSensing
{
    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma);
    
    public void setPosition(float posX, float posY, float posZ);
    
    /**
     * Computes all sensory data for the same time instance
     */
    public void computeAndLockSensorData();
    
    /**
     * Will give the vehicles position at the time that {@code IActuatingSensing.computeAndLockSensorData()} was called.
     * @return 
     */
    public Position2D getPosition();

    public Vector2D getOrientation();

    /**
     * Will give the vehicles center position between rear wheels at the time that {@code IActuatingSensing.computeAndLockSensorData()} was called.
     * @return 
     */
    public Position2D getRearWheelCenterPosition();
    
    /**
     * Will give the vehicles center position between front wheels at the time that {@code IActuatingSensing.computeAndLockSensorData()} was called.
     * @return 
     */
    public Position2D getFrontWheelCenterPosition();

    /**
     * Will give the vehicles length (considered static)
     * @return 
     */
    public double getVehicleLength();

    /**
     * Will set the wheels rotation and steering angle of the car 
     * @param targetWheelRotation
     * @param targetSteeringAngle
     */
    public void drive(float targetWheelRotation, float targetSteeringAngle);

}
