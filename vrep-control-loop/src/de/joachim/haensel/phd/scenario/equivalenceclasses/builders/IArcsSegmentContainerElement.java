package de.joachim.haensel.phd.scenario.equivalenceclasses.builders;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public interface IArcsSegmentContainerElement
{
    public Position2D getStart();
    public Position2D getEnd();
    public List<Line2D> getLines();

    public String toGnuPlotString();
    public String toPyPlotString();
}
