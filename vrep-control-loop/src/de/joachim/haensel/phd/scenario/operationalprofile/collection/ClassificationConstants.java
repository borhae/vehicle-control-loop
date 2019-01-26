package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import de.joachim.haensel.phd.converters.UnitConverter;

public interface ClassificationConstants
{
    public static int setAngleHashValue(double angle)
    {
        return (int)(Math.toDegrees(angle)/10);
    }
    
    public static int angleHashValue(double angle)
    {
        return (int)(Math.toDegrees(angle)/10);
    }
    
    public static int displacementHashValue(double displacement)
    {
        return (int)(displacement/1);
    }
    
    public static int setVelocityHashValue(double velocity)
    {
        return (int)(Math.sqrt(UnitConverter.meterPerSecondToKilometerPerHour(velocity))/2);
    }
    
    public static int velocityHashValue(double velocity)
    {
        return (int)(Math.sqrt(UnitConverter.meterPerSecondToKilometerPerHour(velocity))/2);
    }
}
