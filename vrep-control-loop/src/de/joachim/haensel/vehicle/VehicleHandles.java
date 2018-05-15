package de.joachim.haensel.vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehicleHandles
{
    private int _physicalBody;
    private int _rearLeftWheel;
    private int _rearRightWheel;
    private int _frontLeftWheel;
    private int _frontRightWheel;
    private int _rearWheelDummy;
    private int _damperRearLeft;
    private int _damperRearRight;
    private int _damperFrontLeft;
    private int _damperFrontRight;
    private int _steeringFrontLeft;
    private int _steeringFrontRight;
    private int _connectorDflSfl;
    private int _connectorDrlArl;
    private int _connectorDfrSfr;
    private int _connectorDrrArr;
    private int _connectorSflMfl;
    private int _connectorSfrMfr;
    private int _frontLeftWheelDummy;
    private int _frontRightWheelDummy;
    private int _axisRearLeft;
    private int _axisRearRight;
    private int _motorFrontLeft;
    private int _motorFrontRight;

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

    public VehicleHandles setDamperRearLeft(int damperRearLeft)
    {
        _damperRearLeft = damperRearLeft;
        return this;
    }

    public VehicleHandles setDamperRearRight(int damperRearRight)
    {
        _damperRearRight = damperRearRight;
        return this;
    }

    public VehicleHandles setDamperFrontLeft(int damperFrontLeft)
    {
        _damperFrontLeft = damperFrontLeft;
        return this;
    }

    public VehicleHandles setDamperFrontRight(int damperFrontRight)
    {
        _damperFrontRight = damperFrontRight;
        return this;
    }

    public VehicleHandles setSteeringFrontLeft(int steeringFrontLeft)
    {
        _steeringFrontLeft = steeringFrontLeft;
        return this;
    }

    public VehicleHandles setSteeringFrontRight(int steeringFrontRight)
    {
        _steeringFrontRight = steeringFrontRight;
        return this;
    }
    
    public VehicleHandles setConnectorDflSfl(int connectorDflSfl)
    {
        _connectorDflSfl = connectorDflSfl;
        return this;   
    }
    
    public VehicleHandles setConnectorDfrSfr(int connectorDfrSfr)
    {
        _connectorDfrSfr = connectorDfrSfr;
        return this;   
    }
    
    public VehicleHandles setConnectorDrlArl(int connectorDrlArl)
    {
        _connectorDrlArl = connectorDrlArl;
        return this;   
    }
    
    public VehicleHandles setConnectorDrrArr(int connectorDrrArr)
    {
        _connectorDrrArr = connectorDrrArr;
        return this;   
    }

    public VehicleHandles setConnectorSflMfl(int connectorSflMfl)
    {
        _connectorSflMfl = connectorSflMfl;
        return this;
    }
    
    public VehicleHandles setConnectorSfrMfr(int connectorSfrMfr)
    {
        _connectorSfrMfr = connectorSfrMfr;
        return this;
    }
    
    public VehicleHandles setFrontLeftWheelDummy(int frontLeftWheelDummy)
    {
        _frontLeftWheelDummy = frontLeftWheelDummy;
        return this;
    }

    public VehicleHandles setFrontRightWheelDummy(int frontRightWheelDummy)
    {
        _frontRightWheelDummy = frontRightWheelDummy;
        return this;
    }
    
    public VehicleHandles setAxisRearLeft(int axisRearLeft)
    {
        _axisRearLeft = axisRearLeft;
        return this;
    }
    
    public VehicleHandles setAxisRearRight(int axisRearRight)
    {
        _axisRearRight = axisRearRight;
        return this;
    }
    
    public VehicleHandles setMotorFrontLeft(int motorFrontLeft)
    {
        _motorFrontLeft = motorFrontLeft;
        return this;
    }
    
    public VehicleHandles setMotorFrontRight(int motorFrontRight)
    {
        _motorFrontRight = motorFrontRight;
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

    public List<Integer> getAllHandles()
    {
        List<Integer> result = new ArrayList<>();
        result.add(_physicalBody);
        
        //wheels
        result.add(_rearLeftWheel);
        result.add(_rearRightWheel);
        result.add(_frontLeftWheel);
        result.add(_frontRightWheel);
        
        
        //dampers
        result.add(_damperFrontLeft);
        result.add(_damperFrontRight);
        result.add(_damperRearLeft);
        result.add(_damperRearRight);
        
        // steering
        result.add(_steeringFrontLeft);
        result.add(_steeringFrontRight);
        
        //connectors
        result.add(_connectorDflSfl);
        result.add(_connectorDfrSfr);
        result.add(_connectorDrlArl);
        result.add(_connectorDrrArr);
        result.add(_connectorSflMfl);
        result.add(_connectorSfrMfr);
        
        //axes, motors
        result.add(_axisRearLeft);
        result.add(_axisRearRight);
        result.add(_motorFrontLeft);
        result.add(_motorFrontRight);
        
        //dummies
        result.add(_frontLeftWheelDummy);
        result.add(_frontRightWheelDummy);
        result.add(_rearWheelDummy);
        return result;
    }
}
