package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

public interface IVehicleHandles
{

    int getPhysicalBody();

    IVehicleHandles setPhysicalBody(int physicalBodyHandle);

    IVehicleHandles setRearLeftWheel(int rearLeftWheel);

    IVehicleHandles setRearRightWheel(int rearRightWheel);

    IVehicleHandles setFrontLeftWheel(int frontLeftWheel);

    IVehicleHandles setFrontRightWheel(int frontRightWheel);

    IVehicleHandles setRearWheelVisualizationDummy(int rearWheelDummy);

    IVehicleHandles setDamperRearLeft(int damperRearLeft);

    IVehicleHandles setDamperRearRight(int damperRearRight);

    IVehicleHandles setDamperFrontLeft(int damperFrontLeft);

    IVehicleHandles setDamperFrontRight(int damperFrontRight);

    IVehicleHandles setSteeringFrontLeft(int steeringFrontLeft);

    IVehicleHandles setSteeringFrontRight(int steeringFrontRight);

    IVehicleHandles setConnectorDflSfl(int connectorDflSfl);

    IVehicleHandles setConnectorDfrSfr(int connectorDfrSfr);

    IVehicleHandles setConnectorDrlArl(int connectorDrlArl);

    IVehicleHandles setConnectorDrrArr(int connectorDrrArr);

    IVehicleHandles setConnectorSflMfl(int connectorSflMfl);

    IVehicleHandles setConnectorSfrMfr(int connectorSfrMfr);

    IVehicleHandles setFrontLeftWheelDummy(int frontLeftWheelDummy);

    IVehicleHandles setFrontRightWheelDummy(int frontRightWheelDummy);

    IVehicleHandles setAxisRearLeft(int axisRearLeft);

    IVehicleHandles setAxisRearRight(int axisRearRight);

    IVehicleHandles setMotorFrontLeft(int motorFrontLeft);

    IVehicleHandles setMotorFrontRight(int motorFrontRight);

    int getRearLeftWheel();

    int getRearRightWheel();

    int getFrontLeftWheel();

    int getFrontRightWheel();

    int getRearWheelDummy();

    List<Integer> getAllHandles();

}