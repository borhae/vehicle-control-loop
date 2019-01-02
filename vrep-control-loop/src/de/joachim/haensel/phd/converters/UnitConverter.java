package de.joachim.haensel.phd.converters;

public class UnitConverter
{
    public static double kilometersPerHourToMetersPerSecond(double kilometerPerHour)
    {
        return kilometerPerHour * (10.0 / 36.0);
    }

    public static double meterPerSecondToKilometerPerHour(double meterPerSecond)
    {
        return meterPerSecond * (36.0/10.0);
    }
}
