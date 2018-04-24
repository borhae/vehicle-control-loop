package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;
import de.joachim.haensel.vehicle.ISegmentBuildingListener;

public class Trajectorizer implements ITrajectorizer
{
    private ISegmenter _segmenter;
    private IVelocityAssigner _velocitizer;

    public Trajectorizer(ISegmenterFactory segmenterFactory, IVelocityAssignerFactory velocityAssignerFactory, double segmentSize)
    {
        _segmenter = segmenterFactory.create(segmentSize);
        _velocitizer = velocityAssignerFactory.create(segmentSize);
    }

    @Override
    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        List<Trajectory> result = _segmenter.createSegments(route);
        _velocitizer.addVelocities(result);
        return result;
    }

    @Override
    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> listeners)
    {
        _segmenter.addSegmentBuildingListeners(listeners);
    }
}
