package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.DebugVisualizer;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.EventLoopStateMachine;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.IDebugVisualizer;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.NoOpDebugger;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.TrajectoryBuffer;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public class PurePursuitVariableLookaheadController implements ILowerLayerControl
{
    private EventLoopStateMachine _eventLoopStateMachine;
    
    private List<IArrivedListener> _arrivedListeners;
    private List<ITrajectoryRequestListener> _trajectoryRequestListeners;
    private List<ITrajectoryReportListener> _trajectoryReportListeners;

    private IActuatingSensing _actuatorsSensors;

    private CarInterfaceActions _carInterface;

    private IDebugVisualizer _debugger;

    public PurePursuitVariableLookaheadController()
    {
        _arrivedListeners = new ArrayList<>();
        _trajectoryRequestListeners = new ArrayList<>();
        _trajectoryReportListeners = new ArrayList<>();
    }

    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        TrajectoryBuffer trajectoryBuffer = new TrajectoryBuffer(trajectoryProvider, actuatorsSensors, _trajectoryReportListeners, _trajectoryRequestListeners);
        PurePuresuitTargetProvider targetProvider = new PurePuresuitTargetProvider(trajectoryBuffer, actuatorsSensors);
        _carInterface = new CarInterfaceActions(actuatorsSensors, _arrivedListeners, targetProvider);
        _eventLoopStateMachine = new EventLoopStateMachine(actuatorsSensors, _carInterface, targetProvider);
        _actuatorsSensors = actuatorsSensors;
    }
    
    @Override
    public void controlEvent()
    {
        _eventLoopStateMachine.triggerControlEvent();
    }

    @Override
    public void driveTo(Position2D position)
    {
        _eventLoopStateMachine.triggerDriveToEvent(position);
    }
    
    @Override
    public void stop()
    {
        _eventLoopStateMachine.triggerStopEvent();
    }

    @Override
    public void clearSegmentBuffer()
    {
        _carInterface.clearSegmentBuffer();
    }

    @Override
    public void activateDebugging(IVrepDrawing vrepDrawing, DebugParams params)
    {
        _debugger = new DebugVisualizer(params, vrepDrawing, _actuatorsSensors);
        _carInterface.setDebugger(_debugger);
        
    }

    @Override
    public void deactivateDebugging()
    {
        _carInterface.setDebugger(new NoOpDebugger());
        _debugger.deactivate();
    }

    @Override
    public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener)
    {
        _trajectoryRequestListeners.add(requestListener);
    }

    @Override
    public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
    {
        _trajectoryReportListeners.add(reportListener);
    }
    
    @Override
    public void addArrivedListener(IArrivedListener arrivedListener)
    {
        _arrivedListeners.add(arrivedListener);
    }

    @Override
    public void clearArrivedListeners()
    {
        _arrivedListeners.clear();
    }
}
