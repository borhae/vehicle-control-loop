package de.joachim.haensel.phd.scenario.equivalenceclasses.builders;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Midpoint;

public class LineSegment implements IArcsLineContainerElement
{
    private Line2D _line;

    public LineSegment(Line2D line)
    {
        _line = line;
    }

    public LineSegment(Midpoint mp)
    {
        this(new Line2D(mp.getAssociatedStartPosition(), mp.getAssociatedEndPosition()));
    }

    public Line2D getLine()
    {
        return _line;
    }
}
