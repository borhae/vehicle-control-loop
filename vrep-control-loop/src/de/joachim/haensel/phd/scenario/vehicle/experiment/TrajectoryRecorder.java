package de.joachim.haensel.phd.scenario.vehicle.experiment;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public class TrajectoryRecorder implements ILowerLayerControl, INavigationListener
{

    private IActuatingSensing _actuatorsSensors;
    private List<Position2D> _trajectoryRecord;
    private List<Position2D> _plannedTrajectory;

    public TrajectoryRecorder()
    {
        _trajectoryRecord = new ArrayList<>();
        _plannedTrajectory = new ArrayList<>();
    }
    
    @Override
    public void controlEvent()
    {
        _trajectoryRecord.add(new Position2D(_actuatorsSensors.getPosition()));
    }

    @Override
    public void driveTo(Position2D position)
    {
    }

    @Override
    public void clearSegmentBuffer()
    {
    }

    @Override
    public void clearArrivedListeners()
    {
        // TODO Auto-generated method stub
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

    public List<Position2D> getTrajectory()
    {
        return _trajectoryRecord;
    }

    @Override
    public void notifySegmentsChanged(List<Trajectory> segments)
    {
        List<Position2D> segmentsToAdd = segments.stream().map(trajectory -> trajectory.getVector().getBase()).collect(Collectors.toList());
        if(_plannedTrajectory == null)
        {
            _plannedTrajectory = segmentsToAdd;
        }
        else
        {
            _plannedTrajectory.addAll(segmentsToAdd);
        }
    }
    
    public List<Position2D> getPlannedTrajectory()
    {
        return _plannedTrajectory;
    }

    @Override
    public void notifyRouteChanged(List<Line2D> route)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void activateRouteDebugging()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void activateSegmentDebugging()
    {
        // TODO Auto-generated method stub
        
    }
}
