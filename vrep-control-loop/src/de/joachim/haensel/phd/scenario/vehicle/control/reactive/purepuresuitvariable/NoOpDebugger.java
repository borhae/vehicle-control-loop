package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class NoOpDebugger implements IDebugVisualizer
{
    @Override
    public void showLookaheadElement(TrajectoryElement lookaheadTrajectoryElement)
    {
    }

    @Override
    public void deactivate()
    {
    }

    @Override
    public void showVelocities(float targetWheelRotation, TrajectoryElement lookaheadTrajectoryElement, TrajectoryElement closestTrajectoryElement)
    {
    }
}
