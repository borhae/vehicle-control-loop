package de.joachim.haensel.phd.scenario.experiment.setup;

public class ErrorMsg
{

    private String _verboseMsg;
    private String _shortMsg;
    private boolean _failed;

    public ErrorMsg(String verboseMsg, boolean failed, int idx)
    {
        _verboseMsg = verboseMsg;
        _shortMsg = failed ? String.format("(X: %d, %d)", idx, idx + 1) : "";
        _failed = failed;
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
}
