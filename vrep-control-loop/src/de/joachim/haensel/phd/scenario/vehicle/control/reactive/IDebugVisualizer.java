package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface IDebugVisualizer
{
    public void showLookaheadElement(TrajectoryElement lookaheadTrajectoryElement);

    public void showVelocities(float targetWheelRotation, TrajectoryElement lookaheadTrajectoryElement, TrajectoryElement closestTrajectoryElement);

    public void deactivate();
}
