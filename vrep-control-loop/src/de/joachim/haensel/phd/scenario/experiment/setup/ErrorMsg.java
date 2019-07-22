package de.joachim.haensel.phd.scenario.experiment.setup;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ErrorMsg
{
    private String _verboseMsg;
    private String _shortMsg;
    private boolean _failed;
    private List<TrajectoryElement> _trajectory;
    private int _idx;
    public Position2D curPos;
    public Position2D nexPos;

    public ErrorMsg(String verboseMsg, boolean failed, int idx)
    {
        _verboseMsg = verboseMsg;
        _shortMsg = failed ? String.format("(X: %d, %d)", idx, idx + 1) : "";
        _failed = failed;
    }

    public ErrorMsg(String verboseMsg, boolean failed, int idx, Position2D curPos, Position2D nexPos, List<TrajectoryElement> trajectory)
    {
        this(verboseMsg, failed, idx);
        _trajectory = trajectory;
        _idx = idx;
        this.curPos = curPos;
        this.nexPos = nexPos;
    }


    public boolean isFailed()
    {
        return _failed;
    }

    public String getShortMsg()
    {
        return _shortMsg;
    }

    public String getLongMsg()
    {
        return _verboseMsg;
    }

    public List<TrajectoryElement> getTrajectory()
    {
        return _trajectory;
    }

    public int getIdx()
    {
        return _idx;
    }
}
