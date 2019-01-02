package de.joachim.haensel.phd.scenario.vehicle.experiment;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public class TireBlowOutAtPositionEventGenerator implements ILowerLayerControl
{
    private IActuatingSensing _actuatorsSensors;
    private int _tireRescalings;
    private float[] _tireScaleList;
    private Position2D _blowOutTargetPositon;
    private double _locationPrecision;

    public TireBlowOutAtPositionEventGenerator(Position2D blowOutTargetPositon, double locationPrecision, float reducedTireScale)
    {
        _blowOutTargetPositon = blowOutTargetPositon;
        _tireScaleList = new float[1];
        _tireScaleList[0] = reducedTireScale;
        _tireRescalings = 0;
        _locationPrecision = locationPrecision;
    }

    public TireBlowOutAtPositionEventGenerator(Position2D blowOutTargetPositon, double locationPrecision, float[] tireScaleList)
    {
        _blowOutTargetPositon = blowOutTargetPositon;
        _tireScaleList = tireScaleList;
        _tireRescalings = 0;
        _locationPrecision = locationPrecision;
    }

    @Override
    public void controlEvent()
    {
        if((_actuatorsSensors.getPosition().equals(_blowOutTargetPositon, _locationPrecision)) && (_tireRescalings < _tireScaleList.length))
        {
            _actuatorsSensors.blowTire(3, _tireScaleList[_tireRescalings]);
            System.out.println("blowed tire");
            _tireRescalings++;
        }
        else if((_tireRescalings > 0) && (_tireRescalings < _tireScaleList.length))
        {
            _actuatorsSensors.blowTire(3, _tireScaleList[_tireRescalings]);
            System.out.println("blowed tire");
            _tireRescalings++;
        }
    }

    @Override
    public void driveTo(Position2D position)
    {
        _tireRescalings = 0;
    }

    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        _actuatorsSensors = actuatorsSensors;
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
}
