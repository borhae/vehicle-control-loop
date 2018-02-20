package de.joachim.haensel.vehicle;

public class VehicleHandles
{
    private int _physicalBody;
    private int _rearLeftWheel;
    private int _rearRightWheel;

    public int getPhysicalBody()
    {
        return _physicalBody;
    }

    public VehicleHandles setPhysicalBody(int physicalBodyHandle)
    {
        _physicalBody = physicalBodyHandle;
        return this;
    }

    public VehicleHandles setRearLeftWheel(int rearLeftWheel)
    {
        _rearLeftWheel = rearLeftWheel;
        return this;
    }

    public VehicleHandles setRearRightWheel(int rearRightWheel)
    {
        _rearRightWheel = rearRightWheel;
        return this;
    }

    public int getRearLeftWheel()
    {
        return _rearLeftWheel;
    }
}
