package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.vehicle.ISegmentBuildingListener;

public class Trajectorizer implements ITrajectorizer
{
    private ISegmenter _segmenter;

    public Trajectorizer(ISegmenterFactory segmenterFactory, double segmentSize)
    {
        _segmenter = segmenterFactory.create(segmentSize);
    }

    @Override
    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        List<Trajectory> result = _segmenter.createSegments(route);
        return result;
    }

    @Override
    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> listeners)
    {
        _segmenter.addSegmentBuildingListeners(listeners);
    }
}
