package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;

public class SegmentBuildingAdapter implements IRouteBuildingListener
{
    private Vector2DVisualizer _visualizer;
    private HashMap<SegmentBuildingListenerTypes, VectorContentElement> _listToContentMap;

    public SegmentBuildingAdapter(Vector2DVisualizer visualizer)
    {
        _visualizer = visualizer;
        _listToContentMap = new HashMap<SegmentBuildingListenerTypes, VectorContentElement>();
    }

    @Override
    public void notifyNewRoute(List<Line2D> route)
    {
        Deque<Vector2D> vectors = new LinkedList<>();
        route.forEach(line -> vectors.addFirst(new Vector2D(line)));
        VectorContentElement updateableContent = new VectorContentElement(vectors, Color.BLACK, new BasicStroke(2.0f), 0.5);
        _visualizer.addContentElement(updateableContent);
        _visualizer.updateVisuals();
    }

    @Override
    public void notifyStartOriginalTrajectory(LinkedList<Vector2D> emptyRoute)
    {
        VectorContentElement updateableContent = new VectorContentElement(emptyRoute, Color.ORANGE, new BasicStroke(2.0f), 0.05);
        _visualizer.addContentElement(updateableContent);
        _visualizer.updateVisuals();
        SegmentBuildingListenerTypes.ORIGINAL.setList(emptyRoute);
        _listToContentMap.put(SegmentBuildingListenerTypes.ORIGINAL, updateableContent);
    }

    @Override
    public void notifyStartOverlayTrajectory(Deque<Vector2D> emptyOverlay)
    {
        VectorContentElement updateableContent = new VectorContentElement(emptyOverlay, Color.BLUE, new BasicStroke(2.0f), 0.05);
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
            VectorContentElement elementToUpdate = _listToContentMap.get(SegmentBuildingListenerTypes.ORIGINAL);
            elementToUpdate.addVector(newVector);
            _visualizer.updateVisuals();
        }
        else if (updatedList == SegmentBuildingListenerTypes.OVERLAY.getList())
        {
            VectorContentElement elementToUpdate = _listToContentMap.get(SegmentBuildingListenerTypes.OVERLAY);
            elementToUpdate.addVector(newVector);
            _visualizer.updateVisuals();
        }
    }

    @Override
    public void notifyNewRouteStreetSections(List<IStreetSection> path)
    {
    }
}
