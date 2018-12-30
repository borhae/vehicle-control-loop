package de.joachim.haensel.phd.scenario.tasks;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public interface ITask
{
    public void execute();
    public int getTimeout();
}
