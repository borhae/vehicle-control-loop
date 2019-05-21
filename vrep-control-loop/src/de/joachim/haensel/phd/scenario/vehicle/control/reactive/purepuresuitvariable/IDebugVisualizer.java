package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface IDebugVisualizer
{
    public void showLookaheadElement(TrajectoryElement lookaheadTrajectoryElement);

    public void showVelocities(float targetWheelRotation, TrajectoryElement lookaheadTrajectoryElement, TrajectoryElement closestTrajectoryElement);

    public void deactivate();
}
