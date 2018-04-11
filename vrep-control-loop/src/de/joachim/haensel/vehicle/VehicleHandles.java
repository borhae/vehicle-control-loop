package de.joachim.haensel.vehicle;

public class VehicleHandles
{
    private int _physicalBody;
    private int _rearLeftWheel;
    private int _rearRightWheel;
    private int _frontLeftWheel;
    private int _frontRightWheel;
    private int _rearWheelDummy;

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
    
    public VehicleHandles setFrontLeftWheel(int frontLeftWheel)
    {
        _frontLeftWheel = frontLeftWheel;
        return this;
    }
    
    public VehicleHandles setFrontRightWheel(int frontRightWheel)
    {
        _frontRightWheel = frontRightWheel;
        return this;
    }

    public VehicleHandles setRearWheelVisualizationDummy(int rearWheelDummy)
    {
        _rearWheelDummy = rearWheelDummy;
        return this;
    }
    
    public int getRearLeftWheel()
    {
        return _rearLeftWheel;
    }
    
    public int getRearRightWheel()
    {
        return _rearRightWheel;
    }

    public int getFrontLeftWheel()
    {
        return _frontLeftWheel;
    }

    public int getFrontRightWheel()
    {
        return _frontRightWheel;
    }

    public int getRearWheelDummy()
    {
        return _rearWheelDummy;
    }
}
