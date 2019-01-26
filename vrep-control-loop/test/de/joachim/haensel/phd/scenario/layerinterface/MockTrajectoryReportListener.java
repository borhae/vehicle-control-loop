package de.joachim.haensel.phd.scenario.layerinterface;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;

public class MockTrajectoryReportListener implements ITrajectoryReportListener
{
//    @Override
//    public void notifyEnvironmentState(Position2D rearWheelCenterPosition, Position2D frontWheelCenterPosition, double[] vehicleVelocity, List<IStreetSection> viewAhead, long timeStamp)
//    {
//    }
    @Override
    public void notifyEnvironmentState(Position2D rearWheelCenterPosition, Position2D frontWheelCenterPosition, double[] vehicleVelocity, long timeStamp)
    {
    }
}
