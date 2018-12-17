package de.joachim.haensel.phd.scenario.layerinterface;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public class MockLowerLayerControl implements ILowerLayerControl<Object>
{

    @Override
    public void controlEvent()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void driveTo(Position2D position)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void activateDebugging(IVrepDrawing actuatingSensing, DebugParams params)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivateDebugging()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setParameters(Object parameters)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addArrivedListener(IArrivedListener arrivedListener)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearSegmentBuffer()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearArrivedListeners()
    {
        // TODO Auto-generated method stub
        
    }
}
