package de.joachim.haensel.vehicle;

import java.awt.Color;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public interface IActuatingSensing
{
    /**
     * Computes all sensory data for the same time instance
     */
    public void computeAndLockSensorData();
    
    /**
     * Will give the vehicles position at the time that {@code IActuatingSensing.computeAndLockSensorData()} was called.
     * @return 
     */
    public Position2D getPosition();

    /**
     * Will give the vehicles center position between rear wheels at the time that {@code IActuatingSensing.computeAndLockSensorData()} was called.
     * @return 
     */
    public Position2D getRearWheelCenterPosition();
    
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

    /**
     * For debugging purposes: adds a vector to be drawn (can be updated)
     * @param vector to display
     * @param color the vector should have
     * @return handle, so this can be updated
     */
    public int drawVector(Vector2D vector, Color color);

    /**
     * Update the drawing object formerly created by the {@code drawVector(...)} method
     * @param color to which the drawing object should change
     * @param handle of the drawing object that should be updated 
     * @param vector the new location
     */
    public void drawUpdateVector(int handle, Vector2D vector, Color color);
}
