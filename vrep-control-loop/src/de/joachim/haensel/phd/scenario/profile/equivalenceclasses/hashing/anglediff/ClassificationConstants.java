package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff;

import de.joachim.haensel.phd.converters.UnitConverter;

public interface ClassificationConstants
{
    public static int setAngleHashValue(double angle)
    {
        return (int)(Math.toDegrees(angle)/10);
//        return (int)(Math.log(angle)/10);
    }
    
    public static int angleHashValue(double angle)
    {
        return (int)(Math.toDegrees(angle)/10);
//        return (int)(Math.log(angle)/10);
    }
    
    public static int displacementHashValue(double displacement)
    {
        return (int)(displacement/1);
    }
    
    public static int setVelocityHashValue(double velocity)
    {
        return (int)(UnitConverter.meterPerSecondToKilometerPerHour(velocity)/12);
    }
    
    public static int velocityHashValue(double velocity)
    {
        return (int)(UnitConverter.meterPerSecondToKilometerPerHour(velocity)/12);
    }
}
