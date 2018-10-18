package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Arc;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Segment;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class PartialArc
{
    private List<Position2D> _elements;
    private Arc _arc;
    private Segment _segment;
    private double _iSSETolerance;

    /**
     * Point collector that allows to be transformed into an arc or segment. 
     * Depending on iSSETolerance, either arc or segment recognition will be favored
     * @param iSSETolerance
     */
    public PartialArc(double iSSETolerance)
    {
        _elements = new ArrayList<>();
        _iSSETolerance = iSSETolerance;
    }
    
    public void add(Position2D begin, Position2D end)
    {
        _elements.add(begin);
        _elements.add(end);
    }
    
    public void clear(Position2D startPoint)
    {
        _elements.clear();
        _elements.add(startPoint);
        _arc = null;
        _segment = null;
    }

    /**
     * If the arc's error is smaller than the segments 
     * @return should this become an arc or a segment
     */
    public boolean isArcsISSESmallerThanSegments()
    {
        return _arc.getISSE() < _iSSETolerance * _segment.getISSE();
    }

    /**
     * Builds an arc and a segment from the contained elements.
     * After this method is called, the integral sums of errors (isse) for both are available. 
     */
    public void InitArcAndSegment()
    {
        buildArc();
        buildSegment();
    }

    private void buildSegment()
    {
        _segment = new Segment(new ArrayList<Position2D>(_elements));
        _segment.create(true);
    }

    private void buildArc()
    {
        _arc = new Arc(new ArrayList<Position2D>(_elements));
        _arc.create(true);
    }

    /**
     * Build an Arc from this collection of points
     * Look in the center third for the midpoint that will result in the smallest error between actual points and the found circle
     * @return
     */
    public IArcsSegmentContainerElement toArc()
    {
        if(_arc == null)
        {
            buildArc();
        }
        return _arc;
    }
    
    /**
     * Build a segment from this collection of points
     * @return
     */
    public IArcsSegmentContainerElement toSegment()
    {
        if(_segment == null)
        {
            buildSegment();
        }
        return _segment;
    }

    public boolean isEmpty()
    {
        return _elements.isEmpty();
    }
}
