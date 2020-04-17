package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface ICarInterfaceActionsWithLookahead extends ICarInterfaceActions
{
    public TrajectoryElement getCurrentLookaheadTrajectoryElement();
    public double getCurrentLookahead();
}
