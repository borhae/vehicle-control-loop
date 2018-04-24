package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;

public interface IVelocityAssigner
{
    public void addVelocities(List<Trajectory> result);
}
