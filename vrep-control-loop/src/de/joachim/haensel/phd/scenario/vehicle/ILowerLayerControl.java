package de.joachim.haensel.phd.scenario.vehicle;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public interface ILowerLayerControl<P>
{
    public void controlEvent();
    public void driveTo(Position2D position);
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider);
    public void activateDebugging(IVrepDrawing actuatingSensing, DebugParams params);
    public void deactivateDebugging();
    public void setParameters(P parameters);
    public void stop();
    
    /**
     * Listeners added will receive notifications whenever new trajectories will be requested from the upper
     * layer control interface by the lower layer control.
     * @param requestListener
     */
    public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener);

    /**
     * Listeners added will receive notifications whenever new trajectories will be reported to the upper
     * layer control interface by the lower layer control. 
     * @param requestListener
     */
    public void addTrajectoryReportListener(ITrajectoryReportListener reportListener);

    /**
     * Listeners added will receive a notification if the vehicle arrived at the destination position
     * @param arrivedListener
     */
    public void addArrivedListener(IArrivedListener arrivedListener);
}
