package de.joachim.haensel.phd.scenario.vehicle.control.interfacing;

import java.util.List;

import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public interface ITrajectoryReportListener
{
//    public void notifyEnvironmentState(Position2D rearWheelCenterPosition, Position2D frontWheelCenterPosition, double[] vehicleVelocity, List<IStreetSection> viewAhead, long timeStamp);
    public void notifyEnvironmentState(Position2D rearWheelCenterPosition, Position2D frontWheelCenterPosition, double[] vehicleVelocity, long timeStamp);
}
