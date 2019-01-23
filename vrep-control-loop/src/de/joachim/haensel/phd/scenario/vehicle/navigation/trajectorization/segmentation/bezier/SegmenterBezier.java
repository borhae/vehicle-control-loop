package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.bezier;

import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmentationAlgorithm;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.InterpolationSegmenterBinarySearch;
import de.joachim.haensel.streamextensions.IndexAdder;

/**
 * For now this class is abandoned. Use {@link InterpolationSegmenterBinarySearch} instead.
 * @author dummy
 *
 */
@Deprecated
public class SegmenterBezier implements ISegmentationAlgorithm
{
    private Spline2D _traversableSpline;
    private List<IRouteBuildingListener> _segmentBuildingListeners;

    public Spline2D getTraversableSpline()
    {
        return _traversableSpline;
    }

    @Override
    public void quantize(Deque<Vector2D> source, Deque<Vector2D> result, double stepSize)
    {
        double[][] point = new double[source.size()][];
        source.stream().map(IndexAdder.indexed()).forEach(curElem -> enterInto(point, curElem));
        _traversableSpline = new Spline2D(point);
    }

    private void enterInto(double[][] point, IndexAdder<Vector2D> curElem)
    {
        point[curElem.idx()] = new double[2];
        
        point[curElem.idx()][0] = curElem.v().getbX();
        point[curElem.idx()][1] = curElem.v().getbY();
    }

    @Override
    public void setSegmentBuildingListeners(List<IRouteBuildingListener> segmentBuildingListeners)
    {
        _segmentBuildingListeners = segmentBuildingListeners;
    }
}
