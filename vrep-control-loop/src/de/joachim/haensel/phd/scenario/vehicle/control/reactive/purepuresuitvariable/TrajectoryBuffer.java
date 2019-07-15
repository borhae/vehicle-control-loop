package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;

public class TrajectoryBuffer extends FiniteStateMachineTemplate
{
    private static final int MIN_TRAJECTORY_BUFFER_SIZE = 18;
    private static final int TRAJECTORY_BUFFER_SIZE = 20;

    private ITrajectoryProvider _trajectoryProvider;
    private List<TrajectoryElement> _trajectoryElements;
    private IActuatingSensing _actuatorsSensors;
    private int _currentElementRequestSize;
    private List<ITrajectoryReportListener> _trajectoryReportListeners;
    private List<ITrajectoryRequestListener> _trajectoryRequestListeners;

    public TrajectoryBuffer(ITrajectoryProvider trajectoryProvider, IActuatingSensing actuatorsSensors, List<ITrajectoryReportListener> trajectoryReportListeners, List<ITrajectoryRequestListener> trajectoryRequestListeners)
    {
        _trajectoryProvider = trajectoryProvider;
        _trajectoryElements = new ArrayList<TrajectoryElement>();
        _actuatorsSensors = actuatorsSensors;
        _trajectoryReportListeners = trajectoryReportListeners;
        _trajectoryRequestListeners = trajectoryRequestListeners;

        setInitialState(RouteBufferStates.INIT);
//        Guard enoughElements = () -> _trajectoryElements.size() < MIN_TRAJECTORY_BUFFER_SIZE && _trajectoryProvider.hasElements(_currentElementRequestSize);
//        Guard notEnoughElements = () -> _trajectoryElements.size() < MIN_TRAJECTORY_BUFFER_SIZE && !_trajectoryProvider.hasElements(_currentElementRequestSize);
        Guard enoughElements = () -> _trajectoryElements.size() < MIN_TRAJECTORY_BUFFER_SIZE && _trajectoryProvider.hasElements(_currentElementRequestSize);
        Guard notEnoughElements = () -> _trajectoryElements.size() < MIN_TRAJECTORY_BUFFER_SIZE && !_trajectoryProvider.hasElements(_currentElementRequestSize);

            
        Consumer<Integer> ensureSizeAndReportAction = elementRequestSize -> {ensureSize(elementRequestSize); notifyListeners();};
        Consumer<Integer> ensureSizeAndReportActionInformRouteEndOnScreen = elementRequestSize -> {ensureSize(elementRequestSize); notifyListeners(); System.out.println("buffer route ending");};
        Consumer<Object> routeEndOnScreen = dummy -> System.out.println("buffer route ending");

        
        createTransition(RouteBufferStates.INIT, RouteBufferMsg.ENSURE_SIZE, TRUE_GUARD, RouteBufferStates.ROUTE_ACTIVE, ensureSizeAndReportAction);
        createTransition(RouteBufferStates.ROUTE_ACTIVE, RouteBufferMsg.ENSURE_SIZE, enoughElements, RouteBufferStates.ROUTE_ACTIVE, ensureSizeAndReportAction);
        
        createTransition(RouteBufferStates.ROUTE_ACTIVE, RouteBufferMsg.ENSURE_SIZE, notEnoughElements, RouteBufferStates.ROUTE_ENDING, ensureSizeAndReportActionInformRouteEndOnScreen); //ensure size and report action
        createTransition(RouteBufferStates.ROUTE_ENDING, RouteBufferMsg.ENSURE_SIZE, notEnoughElements, RouteBufferStates.ROUTE_ENDING, routeEndOnScreen); //no op

        createTransition(RouteBufferStates.ROUTE_ENDING, RouteBufferMsg.ENSURE_SIZE, enoughElements, RouteBufferStates.ROUTE_ACTIVE, ensureSizeAndReportActionInformRouteEndOnScreen); //ensure size and report action
        
        reset();
    }

    private void notifyListeners()
    {
        long timeStamp = _actuatorsSensors.getTimeStamp();
        notifyTrajectoryRequestListeners(_trajectoryElements, timeStamp);
        notifyTrajectoryReportListeners(_actuatorsSensors.getRearWheelCenterPosition(), _actuatorsSensors.getFrontWheelCenterPosition(), _actuatorsSensors.getVehicleVelocity(), timeStamp);
    }
    

    private void notifyTrajectoryReportListeners(Position2D rearWheelCenterPosition, Position2D frontWheelCenterPosition, double[] vehicleVelocity, long timeStamp)
    {
        Position2D rWCP = new Position2D(rearWheelCenterPosition);
        Position2D fWCP = new Position2D(frontWheelCenterPosition);
        double[] vel = Arrays.copyOf(vehicleVelocity, vehicleVelocity.length);
        _trajectoryReportListeners.forEach(listener -> listener.notifyEnvironmentState(rWCP, fWCP, vel, timeStamp));
    }
    
//TODO reenable when environment view is part of the observation
//    private void notifyTrajectoryReportListeners(Position2D rearWheelCenterPosition, Position2D frontWheelCenterPosition, double[] vehicleVelocity, List<IStreetSection> viewAhead, long timeStamp)
//    {
//        Position2D rWCP = new Position2D(rearWheelCenterPosition);
//        Position2D fWCP = new Position2D(frontWheelCenterPosition);
//        double[] vel = Arrays.copyOf(vehicleVelocity, vehicleVelocity.length);
//        _trajectoryReportListeners.forEach(listener -> listener.notifyEnvironmentState(rWCP, fWCP, vel, viewAhead, timeStamp));
//    }

    private void notifyTrajectoryRequestListeners(List<TrajectoryElement> trajectories, long timeStamp)
    {
        Runnable copier = () ->   
        {
            List<TrajectoryElement> copy = new ArrayList<>();
            trajectories.forEach(t -> copy.add(t.deepCopy()));
            
            _trajectoryRequestListeners.stream().forEach(listener -> listener.notifyNewTrajectories(copy, timeStamp));
        };

        Thread reportThread = new Thread(copier);
        reportThread.start();
    }

    public void triggerEnsureSize()
    {
        _currentElementRequestSize = TRAJECTORY_BUFFER_SIZE - _trajectoryElements.size();
        transition(RouteBufferMsg.ENSURE_SIZE, Integer.valueOf(_currentElementRequestSize));
    }

    private void ensureSize(Integer elementRequestSize)
    {
        List<TrajectoryElement> trajectories = _trajectoryProvider.getNewElements(elementRequestSize);
        if(trajectories == null)
        {
            System.out.println("Problem: trajectory buffer returns null instead of empty list");
            return;
        }
        
        trajectories.stream().forEach(traj -> _trajectoryElements.add(traj));
    }

    public void clear()
    {
        _trajectoryElements.clear();
    }

    public boolean isEmpty()
    {
        return _trajectoryElements.isEmpty();
    }

    public int size()
    {
        return _trajectoryElements.size();
    }

    public TrajectoryElement get(int index)
    {
        return _trajectoryElements.get(index);
    }

    /**
     * Removes all elements below (not including) the given index from this buffer
     * @param minDistIdx The element at this index will after this operation be pointed to by the zero index
     */
    public void removeBelowIndex(int minDistIdx)
    {
        List<TrajectoryElement> tmp = new ArrayList<>();
        for(int idx = minDistIdx; idx < _trajectoryElements.size(); idx++)
        {
            tmp.add(_trajectoryElements.get(idx));
        }
        _trajectoryElements = tmp;
    }

    public boolean elementsLeft()
    {
        return _trajectoryProvider.segmentsLeft();
    }

    public double getTrajectoryElementLength()
    {
        return _trajectoryProvider.getTrajectoryElementLength();
    }
}
