package de.joachim.haensel.phd.scenario.simulator;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class RoadMapTracker
{
    private RoadMap _roadMap;
    private IStreetSection _activeStreetSection;
    private List<IStreetSection> _activePath;
    private int _activeStreetSectionIdx;

    public RoadMapTracker(IStreetSection closestSection, RoadMap roadMap)
    {
        _activeStreetSection = closestSection;
        _roadMap = roadMap;
    }

    public List<IStreetSection> getViewAhead(double distance, Position2D curPosition, Vector2D lockedOrientation)
    {
        int maxIdx = Math.min(_activeStreetSectionIdx + 10, _activePath.size());
        double minDist = Double.POSITIVE_INFINITY;
        int minDistIdx = Integer.MAX_VALUE;
        for(int idx = _activeStreetSectionIdx; idx < maxIdx; idx++)
        {
            IStreetSection curSection = _activePath.get(idx);
            double curDistance = curSection.getDistance(curPosition);
            if(curDistance < minDist)
            {
                minDistIdx = idx;
                minDist = curDistance;
            }
        }
        if(minDistIdx == Integer.MAX_VALUE)
        {
            return new ArrayList<>();
        }
        else
        {
            _activeStreetSection = _activePath.get(minDistIdx);
            List<IStreetSection> viewAhead = cutToLength(minDistIdx, distance, curPosition);
            return viewAhead;
        }
    }

    private List<IStreetSection> cutToLength(int minDistIdx, double distance, Position2D curPosition)
    {
        List<IStreetSection> result = new ArrayList<>();
        IStreetSection curStreetSection = _activePath.get(minDistIdx);
        int idx = minDistIdx;
        while(curStreetSection.getDistance(curPosition) < distance && idx < _activePath.size())
        {
            result.add(curStreetSection);
            curStreetSection = _activePath.get(idx);
            idx++;
        }
        
        return result;
    }

    public void notifyNewRoute(List<IStreetSection> path)
    {
        _activeStreetSectionIdx = Integer.MAX_VALUE;
        _activePath = expandPath(path);
        for(int idx = 0; idx < _activePath.size(); idx++)
        {
            if(_activePath.get(idx) == _activeStreetSection)
            {
                _activeStreetSectionIdx = idx;
                break;
            }
        }
    }

    private List<IStreetSection> expandPath(List<IStreetSection> path)
    {
        if(path == null || path.isEmpty())
        {
            return path;
        }
        List<IStreetSection> result = new ArrayList<>();
        int idx = 0;
        IStreetSection curSection = path.get(0);
        while(idx < path.size())
        {
            curSection = path.get(idx);
            result.add(curSection);
            if(curSection instanceof Node)
            {
                Node curNode = (Node)curSection;
                if(idx < path.size() -1)
                {
                    IStreetSection nextSection = path.get(idx + 1);
                    if(nextSection instanceof Node)
                    {
                        Node nextNode = (Node)nextSection;
                        Edge incomingEdge = nextNode.getIncomingEdge(curNode);
                        if(incomingEdge != null)
                        {
                            result.add(incomingEdge);
                        }
                    }
                }
            }
            idx++;
        }
        return result;
    }
}
