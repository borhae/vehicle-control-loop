package de.joachim.haensel.phd.scenario.navigation.test;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;

public interface IEdgeLineAdaptor
{
    public void adapt(int curIdx, int nxtIdx, EdgeLine curEdgeLine, EdgeLine nextEdgeLine, Line2D curLine, Line2D nextLine);
}
