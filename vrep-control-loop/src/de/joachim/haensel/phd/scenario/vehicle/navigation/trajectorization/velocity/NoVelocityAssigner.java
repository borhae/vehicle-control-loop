package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.ICurvatureChangeListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.IProfileChangeListener;

public class NoVelocityAssigner implements IVelocityAssigner
{
    @Override
    public void addVelocities(List<TrajectoryElement> result)
    {
    }

    @Override
    public void addProfileChangeListener(IProfileChangeListener listener)
    {
    }

    @Override
    public void addCurvatureChangeListener(ICurvatureChangeListener curveListener)
    {
    }
}
