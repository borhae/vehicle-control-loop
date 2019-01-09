package de.joachim.haensel.phd.scenario.debug;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Point3D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;


/**
 * Will show the navigation result in the simulation
 * @author dummy
 *
 */
public class VRepNavigationListener implements INavigationListener
{
    private VRepObjectCreation _objectCreator;
    private boolean _routeDebugging;
    private boolean _segmentDebugging;

    public enum IDCreator
    {
        INSTANCE;
        
        private Integer _counter = Integer.valueOf(0);
        
        public synchronized Integer getNextID()
        {
            Integer next = Integer.valueOf(_counter.intValue() + 1);
            _counter = next;
            return _counter;
        }

        public synchronized String getNextStringID()
        {
            Integer next = Integer.valueOf(_counter.intValue() + 1);
            _counter = next;
            return _counter.toString();
        }
    }

    public VRepNavigationListener(VRepObjectCreation objectCreator)
    {
        _objectCreator = objectCreator;
        _routeDebugging = false;
        _segmentDebugging = false;
    }

    @Override
    public void notifySegmentsChanged(List<TrajectoryElement> segments)
    {
        if(_segmentDebugging)
        {
            ArrayList<Point3D> vertices = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            segments.forEach(segment -> addSegmentToMesh(segment, vertices, indices));
            try
            {
                _objectCreator.createMeshInSimulation(vertices, indices, "segments" + IDCreator.INSTANCE.getNextStringID(), true);
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        }
    }

    private void addSegmentToMesh(TrajectoryElement segment, ArrayList<Point3D> vertices, ArrayList<Integer> indices)
    {
        double vectorThickness = 1;
        int lastIndex = vertices.size();
        Vector2D v = new Vector2D(segment.getVector());
        double shaftLength = Math.max(v.length() - 2 * vectorThickness, 0.0);
        Vector2D arrowShaft = v.cutLengthFrom(shaftLength);
        if(shaftLength > 0.0)
        {
            Vector2D left = arrowShaft.shift(vectorThickness/2);
            Vector2D right = arrowShaft.shift(-vectorThickness/2);
            vertices.add(new Point3D(left.getBase(), 1.0)); //0
            vertices.add(new Point3D(left.getTip(), 1.0)); //1
            vertices.add(new Point3D(v.getTip(), 1.0)); //2
            vertices.add(new Point3D(right.getTip(), 1.0)); // 3
            vertices.add(new Point3D(right.getBase(), 1.0)); // 4
            
            indices.add(lastIndex + 0);
            indices.add(lastIndex + 1);
            indices.add(lastIndex + 4);
            
            indices.add(lastIndex + 1);
            indices.add(lastIndex + 3);
            indices.add(lastIndex + 4);

            indices.add(lastIndex + 1);
            indices.add(lastIndex + 2);
            indices.add(lastIndex + 3);
        }
    }

    @Override
    public void notifyRouteChanged(List<Line2D> route)
    {
        if(_routeDebugging)
        {
            Color color = new Color(255, 0, 0);
            route.stream().map(IndexAdder.indexed()).forEachOrdered(indexedLine -> {
                try
                {
                    _objectCreator.createLine(indexedLine.v(), 1.0f, 1.0f, 0.5f, "someline_" + indexedLine.idx(), color);
                }
                catch (VRepException exc)
                {
                    exc.printStackTrace();
                }
            });
        }
    }

    @Override
    public void activateRouteDebugging()
    {
        _routeDebugging = true;
    }

    @Override
    public void activateSegmentDebugging()
    {
        _segmentDebugging = true;
    }
}
