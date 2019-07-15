package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;

public class SegmentBuffer
{
    private List<TrajectoryElement> _segments;
    private int _currentIdx;

    public void fillBuffer(List<TrajectoryElement> trajectory)
    {
        _segments = trajectory;
        _currentIdx = 0;
    }

    public List<TrajectoryElement> getSegments(int requestSize)
    {
        int lowerIdx = _currentIdx;
        int higherIdx = Math.min(lowerIdx + requestSize, _segments.size());
        int advance = higherIdx - lowerIdx;
        _currentIdx += advance;
        List<TrajectoryElement> result = new ArrayList<>();
        for( int idx = lowerIdx; idx < higherIdx; idx++)
        {
            result.add(_segments.get(idx));
        }
        return result;
    }

    public TrajectoryElement peek()
    {
        return _segments.get(_currentIdx);
    }

    public int getSize()
    {
        return _segments.size() - _currentIdx;
    }
}
