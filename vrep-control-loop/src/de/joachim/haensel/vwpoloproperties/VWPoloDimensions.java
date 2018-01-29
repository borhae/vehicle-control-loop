package de.joachim.haensel.vwpoloproperties;

public class VWPoloDimensions
{
    //in mm
    private static final double WHEELBASE = 2400;
    private static final double LENGTH = 3715;
    private static final double WIDTH = 1655;
    private static final double HEIGHT = 1420;
    //in kg
    private static final double WEIGHT = 1000;
    
    public static double getWheelbase()
    {
        return WHEELBASE;
    }
    public static double getLength()
    {
        return LENGTH;
    }

    public static double getWidth()
    {
        return WIDTH;
    }

    public static double getHeight()
    {
        return HEIGHT;
    }

    public static double getWeight()
    {
        return WEIGHT;
    }
}
