package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

public interface IVehicleHandles
{
    public IVehicleHandles setPhysicalBody(int physicalBodyHandle);

    public IVehicleHandles setRearLeftWheel(int rearLeftWheel);

    public IVehicleHandles setRearRightWheel(int rearRightWheel);

    public IVehicleHandles setFrontLeftWheel(int frontLeftWheel);

    public IVehicleHandles setFrontRightWheel(int frontRightWheel);

    public IVehicleHandles setRearWheelVisualizationDummy(int rearWheelDummy);

    public IVehicleHandles setDamperRearLeft(int damperRearLeft);

    public IVehicleHandles setDamperRearRight(int damperRearRight);

    public IVehicleHandles setDamperFrontLeft(int damperFrontLeft);

    public IVehicleHandles setDamperFrontRight(int damperFrontRight);

    public IVehicleHandles setSteeringFrontLeft(int steeringFrontLeft);

    public IVehicleHandles setSteeringFrontRight(int steeringFrontRight);

    public IVehicleHandles setConnectorDflSfl(int connectorDflSfl);

    public IVehicleHandles setConnectorDfrSfr(int connectorDfrSfr);

    public IVehicleHandles setConnectorDrlArl(int connectorDrlArl);

    public IVehicleHandles setConnectorDrrArr(int connectorDrrArr);

    public IVehicleHandles setConnectorSflMfl(int connectorSflMfl);

    public IVehicleHandles setConnectorSfrMfr(int connectorSfrMfr);

    public IVehicleHandles setFrontLeftWheelDummy(int frontLeftWheelDummy);

    public IVehicleHandles setFrontRightWheelDummy(int frontRightWheelDummy);

    public IVehicleHandles setAxisRearLeft(int axisRearLeft);

    public IVehicleHandles setAxisRearRight(int axisRearRight);

    public IVehicleHandles setMotorFrontLeft(int motorFrontLeft);

    public IVehicleHandles setMotorFrontRight(int motorFrontRight);
    
    public IVehicleHandles setCtrlScript(int scriptAssociatedWithObject);

    public void setAdditionalObjectHandles(List<Integer> handlesToBeRemoved);
    
    public int getPhysicalBody();

    public int getRearLeftWheel();

    public int getRearRightWheel();

    public int getFrontLeftWheel();

    public int getFrontRightWheel();

    public int getRearWheelDummy();

    public List<Integer> getAllObjectHandles();

    public List<Integer> getAllScriptHandles();
}