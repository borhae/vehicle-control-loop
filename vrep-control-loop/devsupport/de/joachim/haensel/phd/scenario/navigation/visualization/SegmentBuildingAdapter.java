package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.vehicle.ISegmentBuildingListener;

public class SegmentBuildingAdapter implements ISegmentBuildingListener
{
    private Vector2DVisualizer _visualizer;
    private HashMap<SegmentBuildingListenerTypes, ContentElement> _listToContentMap;

    public SegmentBuildingAdapter(Vector2DVisualizer visualizer)
    {
        _visualizer = visualizer;
        _listToContentMap = new HashMap<SegmentBuildingListenerTypes, ContentElement>();
    }

    @Override
    public void notifyNewRoute(List<Line2D> route)
    {
        Deque<Vector2D> vectors = new LinkedList<>();
        route.forEach(line -> vectors.addFirst(new Vector2D(line)));
        ContentElement updateableContent = new ContentElement(vectors, Color.BLACK, new BasicStroke(2.0f), 0.5);
        _visualizer.addContentElement(updateableContent);
        _visualizer.updateVisuals();
    }

    @Override
    public void notifyStartOriginalTrajectory(LinkedList<Vector2D> emptyRoute)
    {
        ContentElement updateableContent = new ContentElement(emptyRoute, Color.ORANGE, new BasicStroke(2.0f), 0.05);
        _visualizer.addContentElement(updateableContent);
        _visualizer.updateVisuals();
        SegmentBuildingListenerTypes.ORIGINAL.setList(emptyRoute);
        _listToContentMap.put(SegmentBuildingListenerTypes.ORIGINAL, updateableContent);
    }

    @Override
    public void notifyStartOverlayTrajectory(Deque<Vector2D> emptyOverlay)
    {
        ContentElement updateableContent = new ContentElement(emptyOverlay, Color.BLUE, new BasicStroke(2.0f), 0.05);
        _visualizer.addContentElement(updateableContent);
        _visualizer.updateVisuals();
        SegmentBuildingListenerTypes.OVERLAY.setList(emptyOverlay);
        _listToContentMap.put(SegmentBuildingListenerTypes.OVERLAY, updateableContent);
    }

    @Override
    public void updateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList)
    {
        if(updatedList == SegmentBuildingListenerTypes.ORIGINAL.getList())
        {
            ContentElement elementToUpdate = _listToContentMap.get(SegmentBuildingListenerTypes.ORIGINAL);
            elementToUpdate.addVector(newVector);
            _visualizer.updateVisuals();
        }
        else if (updatedList == SegmentBuildingListenerTypes.OVERLAY.getList())
        {
            ContentElement elementToUpdate = _listToContentMap.get(SegmentBuildingListenerTypes.OVERLAY);
            elementToUpdate.addVector(newVector);
            _visualizer.updateVisuals();
        }
    }
}
