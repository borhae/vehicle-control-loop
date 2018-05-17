package de.joachim.haensel.phd.scenario.vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehicleHandles implements IVehicleHandles 
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
    private int _ctrlScript;

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#getPhysicalBody()
     */
    @Override
    public int getPhysicalBody()
    {
        return _physicalBody;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setPhysicalBody(int)
     */
    @Override
    public IVehicleHandles setPhysicalBody(int physicalBodyHandle)
    {
        _physicalBody = physicalBodyHandle;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setRearLeftWheel(int)
     */
    @Override
    public IVehicleHandles setRearLeftWheel(int rearLeftWheel)
    {
        _rearLeftWheel = rearLeftWheel;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setRearRightWheel(int)
     */
    @Override
    public IVehicleHandles setRearRightWheel(int rearRightWheel)
    {
        _rearRightWheel = rearRightWheel;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setFrontLeftWheel(int)
     */
    @Override
    public IVehicleHandles setFrontLeftWheel(int frontLeftWheel)
    {
        _frontLeftWheel = frontLeftWheel;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setFrontRightWheel(int)
     */
    @Override
    public IVehicleHandles setFrontRightWheel(int frontRightWheel)
    {
        _frontRightWheel = frontRightWheel;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setRearWheelVisualizationDummy(int)
     */
    @Override
    public IVehicleHandles setRearWheelVisualizationDummy(int rearWheelDummy)
    {
        _rearWheelDummy = rearWheelDummy;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setDamperRearLeft(int)
     */
    @Override
    public IVehicleHandles setDamperRearLeft(int damperRearLeft)
    {
        _damperRearLeft = damperRearLeft;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setDamperRearRight(int)
     */
    @Override
    public IVehicleHandles setDamperRearRight(int damperRearRight)
    {
        _damperRearRight = damperRearRight;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setDamperFrontLeft(int)
     */
    @Override
    public IVehicleHandles setDamperFrontLeft(int damperFrontLeft)
    {
        _damperFrontLeft = damperFrontLeft;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setDamperFrontRight(int)
     */
    @Override
    public IVehicleHandles setDamperFrontRight(int damperFrontRight)
    {
        _damperFrontRight = damperFrontRight;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setSteeringFrontLeft(int)
     */
    @Override
    public IVehicleHandles setSteeringFrontLeft(int steeringFrontLeft)
    {
        _steeringFrontLeft = steeringFrontLeft;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setSteeringFrontRight(int)
     */
    @Override
    public IVehicleHandles setSteeringFrontRight(int steeringFrontRight)
    {
        _steeringFrontRight = steeringFrontRight;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setConnectorDflSfl(int)
     */
    @Override
    public IVehicleHandles setConnectorDflSfl(int connectorDflSfl)
    {
        _connectorDflSfl = connectorDflSfl;
        return this;   
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setConnectorDfrSfr(int)
     */
    @Override
    public IVehicleHandles setConnectorDfrSfr(int connectorDfrSfr)
    {
        _connectorDfrSfr = connectorDfrSfr;
        return this;   
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setConnectorDrlArl(int)
     */
    @Override
    public IVehicleHandles setConnectorDrlArl(int connectorDrlArl)
    {
        _connectorDrlArl = connectorDrlArl;
        return this;   
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setConnectorDrrArr(int)
     */
    @Override
    public IVehicleHandles setConnectorDrrArr(int connectorDrrArr)
    {
        _connectorDrrArr = connectorDrrArr;
        return this;   
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setConnectorSflMfl(int)
     */
    @Override
    public IVehicleHandles setConnectorSflMfl(int connectorSflMfl)
    {
        _connectorSflMfl = connectorSflMfl;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setConnectorSfrMfr(int)
     */
    @Override
    public IVehicleHandles setConnectorSfrMfr(int connectorSfrMfr)
    {
        _connectorSfrMfr = connectorSfrMfr;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setFrontLeftWheelDummy(int)
     */
    @Override
    public IVehicleHandles setFrontLeftWheelDummy(int frontLeftWheelDummy)
    {
        _frontLeftWheelDummy = frontLeftWheelDummy;
        return this;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setFrontRightWheelDummy(int)
     */
    @Override
    public IVehicleHandles setFrontRightWheelDummy(int frontRightWheelDummy)
    {
        _frontRightWheelDummy = frontRightWheelDummy;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setAxisRearLeft(int)
     */
    @Override
    public IVehicleHandles setAxisRearLeft(int axisRearLeft)
    {
        _axisRearLeft = axisRearLeft;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setAxisRearRight(int)
     */
    @Override
    public IVehicleHandles setAxisRearRight(int axisRearRight)
    {
        _axisRearRight = axisRearRight;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setMotorFrontLeft(int)
     */
    @Override
    public IVehicleHandles setMotorFrontLeft(int motorFrontLeft)
    {
        _motorFrontLeft = motorFrontLeft;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#setMotorFrontRight(int)
     */
    @Override
    public IVehicleHandles setMotorFrontRight(int motorFrontRight)
    {
        _motorFrontRight = motorFrontRight;
        return this;
    }

    public IVehicleHandles setCtrlScript(int ctrlScript)
    {
        _ctrlScript = ctrlScript;
        return this;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#getRearLeftWheel()
     */
    @Override
    public int getRearLeftWheel()
    {
        return _rearLeftWheel;
    }
    
    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#getRearRightWheel()
     */
    @Override
    public int getRearRightWheel()
    {
        return _rearRightWheel;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#getFrontLeftWheel()
     */
    @Override
    public int getFrontLeftWheel()
    {
        return _frontLeftWheel;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#getFrontRightWheel()
     */
    @Override
    public int getFrontRightWheel()
    {
        return _frontRightWheel;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#getRearWheelDummy()
     */
    @Override
    public int getRearWheelDummy()
    {
        return _rearWheelDummy;
    }

    /* (non-Javadoc)
     * @see de.joachim.haensel.vehicle.IVehicleHandles#getAllHandles()
     */
    @Override
    public List<Integer> getAllObjectHandles()
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

    @Override
    public List<Integer> getAllScriptHandles()
    {
        List<Integer> result = new ArrayList<>();
        //scripts
        result.add(_ctrlScript);
        return result;
    }
}
