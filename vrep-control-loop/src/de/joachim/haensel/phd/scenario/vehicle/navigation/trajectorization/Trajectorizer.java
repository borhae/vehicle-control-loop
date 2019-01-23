package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;

public class Trajectorizer implements ITrajectorizer
{
    private ISegmenter _segmenter;
    private IVelocityAssigner _velocityAssigner;

    public Trajectorizer(ISegmenterFactory segmenterFactory, IVelocityAssignerFactory velocityAssignerFactory, double segmentSize)
    {
        _segmenter = segmenterFactory.create(segmentSize);
        _velocityAssigner = velocityAssignerFactory.create(segmentSize);
    }

    @Override
    public List<TrajectoryElement> createTrajectory(List<Line2D> route)
    {
        List<TrajectoryElement> result = _segmenter.createSegments(route);
        _velocityAssigner.addVelocities(result);
        return result;
    }

    @Override
    public void addSegmentBuildingListeners(List<IRouteBuildingListener> listeners)
    {
        _segmenter.addSegmentBuildingListeners(listeners);
    }

    @Override
    public IVelocityAssigner getVelocityAssigner()
    {
        return _velocityAssigner;
    }
}
