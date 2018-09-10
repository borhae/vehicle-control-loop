package de.joachim.haensel.phd.scenario.equivalenceclasses.builders;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Midpoint;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class Segment implements IArcsSegmentContainerElement
{
    private static final double NOT_SET = -1.0;
    private Vector2D _segment;
    private List<Position2D> _elements;
    private double _iSSE;

    public Segment(Line2D line)
    {
        _segment = new Vector2D(line);
        _iSSE = NOT_SET;
    }

    public Segment(Midpoint mp)
    {
        this(new Line2D(mp.getAssociatedStartPosition(), mp.getAssociatedEndPosition()));
    }

    public Segment(List<Position2D> elements)
    {
        _elements = elements;
        _iSSE = NOT_SET;
    }

    public Line2D getLine()
    {
        return new Line2D(_segment.getBase(), _segment.getTip());
    }

    /**
     * Create the curve from  elements this was initialized with.
     * @param computeIntegralSumOfErrors whether the integral sum of errors is needed
     */
    public void create(boolean computeIntegralSumOfErrors)
    {
        _segment = new Vector2D(_elements.get(0), _elements.get(_elements.size() - 1));
        if(computeIntegralSumOfErrors)
        {
            computeISSE();
        }
    }

    private void computeISSE()
    {
        _iSSE = 0;
        for (Position2D curPos : _elements)
        {
            double distance = _segment.unboundedDistance(curPos);
            _iSSE += distance * distance;
        }
    }

    public double getISSE()
    {
        if(_iSSE == NOT_SET)
        {
            computeISSE();
        }
        return _iSSE;
    }
}
