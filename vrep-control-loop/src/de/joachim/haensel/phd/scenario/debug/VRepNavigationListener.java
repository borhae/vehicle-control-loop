package de.joachim.haensel.phd.scenario.debug;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Point3D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;


/**
 * Will show the navigation result in the simulation
 * @author dummy
 *
 */
public class VRepNavigationListener implements INavigationListener
{
    public interface IIDCreator
    {
        public String getNextStringID();
    }

    private VRepObjectCreation _objectCreator;
    private boolean _routeDebugging;
    private boolean _segmentDebugging;
    private IIDCreator _idCreator;
    private boolean _routeEndsDebugging;

    public enum IDCreator implements IIDCreator
    {
        INSTANCE;
        
        private Integer _counter = Integer.valueOf(0);
        
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
        _routeEndsDebugging = false;
        _idCreator = IDCreator.INSTANCE;
    }
    
    public VRepNavigationListener(VRepObjectCreation objectCreator, IIDCreator idCreator)
    {
        this(objectCreator);
        _idCreator = idCreator;
    }

    @Override
    public void notifySegmentsChanged(List<TrajectoryElement> segments, Position2D startPos, Position2D endPos)
    {
        String routeID = _idCreator.getNextStringID();
        if(_segmentDebugging)
        {
            ArrayList<Point3D> vertices = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            segments.forEach(segment -> addSegmentToMesh(segment, vertices, indices));
            try
            {
                _objectCreator.createMeshInSimulation(vertices, indices, "segments" + routeID, true);
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        }
        if(_routeEndsDebugging && segments.size() >= 2)
        {
//            Position2D startPos = segments.get(0).getVector().getBase();
//            Position2D endPos = segments.get(segments.size() - 1).getVector().getTip();
            try
            {
                ShapeParameters startSphereParams = new ShapeParameters();
                startSphereParams.makeStandardSphere();
                startSphereParams.setName("start_" + routeID);
                startSphereParams.setPosition((float)startPos.getX(), (float)startPos.getY(), 1.0f);
                _objectCreator.createPrimitive(startSphereParams);

                ShapeParameters endSphereParams = new ShapeParameters();
                endSphereParams.makeStandardSphere();
                endSphereParams.setName("end_" + routeID);
                endSphereParams.setPosition((float)endPos.getX(), (float)endPos.getY(), 1.0f);
                _objectCreator.createPrimitive(endSphereParams);
            } 
            catch (VRepException e)
            {
                e.printStackTrace();
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
    
    public void activateRouteEndsDebugging()
    {
        _routeEndsDebugging = true;
    }
}
