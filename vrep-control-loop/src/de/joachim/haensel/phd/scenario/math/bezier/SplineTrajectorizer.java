package de.joachim.haensel.phd.scenario.math.bezier;

import java.util.Deque;

import de.joachim.haensel.phd.scenario.math.interpolation.IterativeInterpolationTrajectorizer;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.AbstractTrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.streamextensions.IndexAdder;

/**
 * For now this class is abandoned. Use {@link IterativeInterpolationTrajectorizer} instead.
 * @author dummy
 *
 */
@Deprecated
public class SplineTrajectorizer extends AbstractTrajectorizer implements ITrajectorizer
{
    private Spline2D _traversableSpline;

    public SplineTrajectorizer(double stepSize)
    {
        super(stepSize);
    }

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
}
