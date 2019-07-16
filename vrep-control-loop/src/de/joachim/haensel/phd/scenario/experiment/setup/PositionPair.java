package de.joachim.haensel.phd.scenario.experiment.setup;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class PositionPair 
{
	public Position2D curPos;
	public Position2D nexPos;
	public int idx;
    public List<TrajectoryElement> result;

	public PositionPair(Position2D curPos, Position2D nexPos, int idx) 
	{
		this.curPos = curPos;
		this.nexPos = nexPos;
		this.idx = idx;
		this.result = null;
	}
}
