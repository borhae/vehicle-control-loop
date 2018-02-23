package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.sumo2vrep.Line2D;

public abstract class AbstractTrajectorizer implements ITrajectorizer
{
    protected float[][] _points;

    protected void routeToPointArray(List<Line2D> route)
    {
        _points = new float[route.size() + 1][2];
        route.stream().map(IndexAdder.indexed())
                .forEach(indexedLine -> enterInto(_points, indexedLine.index(), indexedLine.value()));
    }

    private void enterInto(float[][] points, int idx, Line2D line)
    {
        points[idx][0] = line.getX1();
        points[idx][1] = line.getY1();
    }

    @Override
    public float[][] getPoints()
    {
        return _points;
    }
    
    @Override 
    public abstract List<Trajectory> createTrajectory(List<Line2D> route);
}