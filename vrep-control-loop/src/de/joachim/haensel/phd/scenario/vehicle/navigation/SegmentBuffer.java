package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

public class SegmentBuffer
{
    private List<Trajectory> _segments;
    private int _currentIdx;

    public void fillBuffer(List<Trajectory> trajectory)
    {
        _segments = trajectory;
        _currentIdx = 0;
    }

    public List<Trajectory> getSegments(int requestSize)
    {
        int lowerIdx = _currentIdx;
        int higherIdx = Math.min(lowerIdx + requestSize, _segments.size());
        int advance = higherIdx - lowerIdx;
        _currentIdx += advance;
        return _segments.subList(lowerIdx, higherIdx);
    }

    public Trajectory peek()
    {
        return _segments.get(0);
    }

    public int getSize()
    {
        return _segments.size();
    }
}
