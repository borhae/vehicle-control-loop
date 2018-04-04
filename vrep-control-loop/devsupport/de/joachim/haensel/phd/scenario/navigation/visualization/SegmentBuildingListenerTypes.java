package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.util.Deque;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;

public enum SegmentBuildingListenerTypes
{
    OVERLAY , ORIGINAL;

    private Deque<Vector2D> _list;
    
    private SegmentBuildingListenerTypes()
    {
    }
    
    public void setList(Deque<Vector2D> list)
    {
        _list = list;
    }
    
    public Deque<Vector2D> getList()
    {
        return _list;
    }
}
