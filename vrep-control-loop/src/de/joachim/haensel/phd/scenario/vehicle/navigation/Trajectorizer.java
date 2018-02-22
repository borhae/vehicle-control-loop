package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.bezier.Spline2D;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.RoadMap;

public class Trajectorizer
{
    private Spline2D _traversableSpline;
    private float[][] _points;

    public Trajectorizer(
)
    {
    }

    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        List<Trajectory> result = new ArrayList<>();
        _points = new float[route.size()+1][2];
        route.stream().map(IndexAdder.indexed()).forEach(indexedLine -> enterInto(_points, indexedLine.index(), indexedLine.value()));
        int lastIndex = _points.length - 1;
        _points[lastIndex][0] = route.get(route.size() - 1).getX2();
        _points[lastIndex][1] = route.get(route.size() - 1).getY2();
        _traversableSpline = new Spline2D(_points);
        //TODO not ready here, trajectory not built yet
        return result;
    }

    private void enterInto(float[][] points, int idx, Line2D line)
    {
        points[idx][0] = line.getX1();
        points[idx][1] = line.getY1();
    }

    public Spline2D getTraversableSpline()
    {
        return _traversableSpline;
    }

    public float[][] getPoints()
    {
        return _points;
    }
}
