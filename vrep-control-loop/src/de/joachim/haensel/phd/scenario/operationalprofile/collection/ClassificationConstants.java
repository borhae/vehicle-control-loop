package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import de.joachim.haensel.phd.converters.UnitConverter;

public interface ClassificationConstants
{
    public static int CURRENT_VELOCITY_RANGE = 240; // unit: km/h
    public static int VELOCITY_RANGE_NUM_OF_CLASSES = 20;
    public static int CURRENT_VELOCITY_DIVISOR = CURRENT_VELOCITY_RANGE / VELOCITY_RANGE_NUM_OF_CLASSES; // unit: km/h
    
    public static int setAngleHashValue(double angle)
    {
        return (int)(Math.toDegrees(angle)/100);
//        return 1;
    }
    
    public static int angleHashValue(double angle)
    {
        return (int)(Math.toDegrees(angle)/100);
//        return 1;
    }
    
    public static int displacementHashValue(double displacement)
    {
        return (int)(displacement/1);
//        return 1;
    }
    
    public static int setVelocityHashValue(double velocity)
    {
        return (int)(UnitConverter.meterPerSecondToKilometerPerHour(velocity)/20);
//        return 1;
    }
    
    public static int velocityHashValue(double velocity)
    {
        return (int)(UnitConverter.meterPerSecondToKilometerPerHour(velocity)/20);
//        return 1;
    }
}
