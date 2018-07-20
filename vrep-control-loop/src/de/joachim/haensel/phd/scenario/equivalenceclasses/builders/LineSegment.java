package de.joachim.haensel.phd.scenario.equivalenceclasses.builders;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;

public class LineSegment implements IArcsLineContainerElement
{
    private Line2D _line;

    public LineSegment(Line2D line)
    {
        _line = line;
    }
}
