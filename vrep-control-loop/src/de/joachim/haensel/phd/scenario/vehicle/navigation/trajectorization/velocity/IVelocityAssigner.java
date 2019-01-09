package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.ICurvatureChangeListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.BasicVelocityAssigner.IProfileChangeListener;

public interface IVelocityAssigner
{
    public void addVelocities(List<TrajectoryElement> result);

    public void addProfileChangeListener(IProfileChangeListener listener);

    public void addCurvatureChangeListener(ICurvatureChangeListener curveListener);
}
