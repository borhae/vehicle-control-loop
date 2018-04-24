package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity;

public interface IVelocityAssignerFactory
{
    public IVelocityAssigner create(double segmentSize);
}
