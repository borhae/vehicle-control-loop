package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

public class VehicleWithCameraHandles extends VehicleHandles
{
    private int _cameraHandle;

    public void setCamera(int cameraHandle)
    {
        _cameraHandle = cameraHandle;
    }

    @Override
    public List<Integer> getAllHandles()
    {
        List<Integer> handles = super.getAllHandles();
        handles.add(_cameraHandle);
        return handles;
    }
}
