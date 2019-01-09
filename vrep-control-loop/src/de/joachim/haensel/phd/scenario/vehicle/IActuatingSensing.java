package de.joachim.haensel.phd.scenario.vehicle;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public interface IActuatingSensing
{
    public static final double INVALID_WHEEL_DIAMETER = Double.NaN;

    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma);
    
    public void setPosition(float posX, float posY, float posZ);
    
    /**
     * Computes all sensory data for the same time instance
     */
    public void computeAndLockSensorData();
    
    /**
     * Get's the current time in the simulator in milliseconds 
     * @return
     */
    public long getTimeStamp();

    /**
     * Simulates the blow up of a tire
     * @param tiresToBlow the tire to blow: set to true at the following index:1 front left, 2 front right, 3 back right, 4 back left
     * @param tireScale blown wheel size
     */
    public void blowTire(boolean[] tiresToBlow, float tireScale);
 
    /**
     * Returns the cars orientation vector
     * @return
     */
    public Vector2D getOrientation();
    
    /**
     * This will gives 
     * Will give the vehicles position at the time that {@code IActuatingSensing.computeAndLockSensorData()} was called.
     * @return 
     */
    public Position2D getPosition();

    /**
     * Use this when simulation is not running!
     * @return
     */
    public Position2D getNonDynamicPosition();

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
     * will return the diameter of a wheel that actuates the vehicle
     */
    public double getWheelDiameter();
    
    /**
     * Returns the vehicles measured velocity in simulation (in objects x, y and z direction)
     * @return
     */
    public double[] getVehicleVelocity();

    /**
     * Will set the wheels rotation and steering angle of the car 
     * @param targetWheelRotation
     * @param targetSteeringAngle
     */
    public void drive(float targetWheelRotation, float targetSteeringAngle);

    /**
     * Initialize the connection to the real world - simulator
     */
    public void initialize();
}
