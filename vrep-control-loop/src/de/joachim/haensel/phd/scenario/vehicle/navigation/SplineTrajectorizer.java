package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.bezier.Spline2D;
import de.joachim.haensel.sumo2vrep.Line2D;

/**
 * For now this class is abandoned. Use {@link IterativeInterpolationTrajectorizer} instead.
 * @author dummy
 *
 */
@Deprecated
public class SplineTrajectorizer extends AbstractTrajectorizer implements ITrajectorizer
{
    private Spline2D _traversableSpline;

    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        List<Trajectory> result = new ArrayList<>();
        _points = routeToPointArray(route);
        int lastIndex = _points.length - 1;
        _points[lastIndex][0] = route.get(route.size() - 1).getX2();
        _points[lastIndex][1] = route.get(route.size() - 1).getY2();
        _traversableSpline = new Spline2D(_points);
        // TODO not ready here, trajectory not built yet
        return result;
    }

    public Spline2D getTraversableSpline()
    {
        return _traversableSpline;
    }
}
