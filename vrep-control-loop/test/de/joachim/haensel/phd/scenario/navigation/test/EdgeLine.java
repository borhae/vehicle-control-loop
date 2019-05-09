package de.joachim.haensel.phd.scenario.navigation.test;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import sumobindings.EdgeType;

public class EdgeLine
{
    private Line2D _line;
    private EdgeType _edge;
    private boolean _sharpTurn;
    private Position2D _center;
    private boolean _isMultiLoopPart;
    private EdgeLine _otherMultiLoopPart;
    private boolean _startOfSharpTurn;
    private boolean _endOfSharpTurn;
    private EdgeLine _nextEdgeLine;

    public EdgeLine(Line2D line, EdgeType edge)
    {
        _line = line;
        _edge = edge;
        _sharpTurn = false;
        _isMultiLoopPart = false;
        _startOfSharpTurn = false;
        _endOfSharpTurn = false;
        _nextEdgeLine = null;
    }
    
    public EdgeLine getNextEdgeLine()
    {
        return _nextEdgeLine;
    }

    public boolean isSharpTurn()
    {
        return _sharpTurn;
    }

    public Line2D getLine()
    {
        return _line;
    }

    public EdgeType getEdge()
    {
        return _edge;
    }

    public void markAsPartOfSharpTurn()
    {
        _sharpTurn = true;
    }

    public void setCenter(Position2D center)
    {
        _center = center;
    }

    public Position2D getCenter()
    {
        return _center;
    }

    public void setMultiLoopPart()
    {
        _isMultiLoopPart = true;
    }

    public boolean isMultiLoopPart()
    {
        return _isMultiLoopPart;
    }

    public void setOtherMultiLoopPart(EdgeLine otherMultiLoopPart)
    {
        _otherMultiLoopPart = otherMultiLoopPart;
    }

    public EdgeLine getOtherMultiLoopPart()
    {
        return _otherMultiLoopPart;
    }

    public void markAsStartOfSharpTurn()
    {
        _startOfSharpTurn = true;
    }

    public void markAsEndOfSharpTurn()
    {
        _endOfSharpTurn = true;
    }

    public void setEndOfSharpTurnEdgeLine(EdgeLine nextEdgeLine)
    {
        _nextEdgeLine = nextEdgeLine;
    }
}
