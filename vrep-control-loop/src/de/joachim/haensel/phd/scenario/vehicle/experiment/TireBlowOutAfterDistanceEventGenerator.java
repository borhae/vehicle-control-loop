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

public class TireBlowOutAfterDistanceEventGenerator implements ILowerLayerControl
{
    private double _distanceUntilBlowout;
    private IActuatingSensing _actuatorsSensors;
    private Position2D _startPosition;
    private double _distance;
    private Position2D _lastPosition;
    private float[] _tireScaleList;
    private int _tireRescalings;

    public TireBlowOutAfterDistanceEventGenerator(double distanceUntilBlowout, float reducedTireScale)
    {
        _distanceUntilBlowout = distanceUntilBlowout;
        _lastPosition = null;
        _tireScaleList = new float[1];
        _tireScaleList[0] = reducedTireScale;
        _tireRescalings = 0;
    }

    public TireBlowOutAfterDistanceEventGenerator(int distanceUntilBlowout, float[] tireScaleList)
    {
        _distanceUntilBlowout = distanceUntilBlowout;
        _lastPosition = null;
        _tireScaleList = tireScaleList;
        _tireRescalings = 0;
    }

    @Override
    public void controlEvent()
    {
        if(_startPosition != null)
        {
            if((_distance > _distanceUntilBlowout) && (_tireRescalings < _tireScaleList.length))
            {
                _actuatorsSensors.blowTire(new boolean[]{false, false, true, false}, _tireScaleList[_tireRescalings]);
                System.out.println("blowed tire");
                _tireRescalings++;
            }
            else
            {
                if(_lastPosition == null)
                {
                    _lastPosition = _startPosition;
                }
                Position2D currentPos = new Position2D(_actuatorsSensors.getPosition());
                double distance = _lastPosition.distance(currentPos);
                _distance += distance;
//                System.out.println("distance" + _distance);
                _lastPosition = currentPos;
            }
        }
    }

    @Override
    public void driveTo(Position2D position)
    {
        _startPosition = _actuatorsSensors.getPosition();
        _distance = 0.0;
        _lastPosition = null;
        _tireRescalings = 0;
    }

    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        _actuatorsSensors = actuatorsSensors;
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
