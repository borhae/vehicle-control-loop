package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.ArcSegment;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsLineContainerElement;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class PartialArc
{
    private List<Position2D> _elements;

    public PartialArc()
    {
        _elements = new ArrayList<>();
    }
    
    public void add(Position2D begin, Position2D end)
    {
        _elements.add(begin);
        _elements.add(end);
    }

    public IArcsLineContainerElement toArcSegment()
    {
        return new ArcSegment();
    }

    public void clear()
    {
        _elements.clear();
    }
}
