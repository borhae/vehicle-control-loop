package de.joachim.haensel.phd.scenario.tasks.creation;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.tasks.DriveAtoBTask;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;

public class FixedSourceTargetContinuousRouteTaskCreatorConfig implements ITaskCreatorConfig
{
    private int _numOfTasks;
    private Position2D _source;
    private Position2D _target;
    private int _current;
    private List<Line2D> _route;
    private int _stepSize;

    public FixedSourceTargetContinuousRouteTaskCreatorConfig(int numOfTasks)
    {
        _numOfTasks = numOfTasks;
    }

    @Override
    public DriveAtoBTask getNext()
    {
        Position2D source;
        Position2D target;
        if(_current + _stepSize < _route.size())
        {
            source = _route.get(_current).getP1();
            target = _route.get(_current + _stepSize).getP2();
        }
        else
        {
            if(_current < _route.size())
            {
                source = _route.get(_current).getP1();
                target = _route.get(_route.size() - 1).getP2();
            }
            else
            {
                return null;
            }
        }
        _current += _stepSize;
        int timeoutSec = ITaskCreatorConfig.estimateTimeout(source, target);
        // TODO add vehicle creation tasks in init
//        return new DriveAtoBTask(source, target, timeoutSec );
        return null;
    }

    public void setSourceTarget(Position2D source, Position2D target, RoadMap map)
    {
        _source = map.getClosestPointOnMap(source);
        _target = map.getClosestPointOnMap(target);
        Navigator navigator = new Navigator(map);
        _route = navigator.getRoute(_source, _target);
        _current = 0;
        _stepSize = _route.size() / _numOfTasks;
    }

    public void setSourceTarget(double xSource, double ySource, double xTarget, double yTarget, RoadMap map)
    {
        _source = map.getClosestPointOnMap(new Position2D(xSource, ySource));
        _target = map.getClosestPointOnMap(new Position2D(xTarget, yTarget));
        Navigator navigator = new Navigator(map);
        _route = navigator.getRoute(_source, _target);
        _current = 0;
        _stepSize = _route.size() / _numOfTasks;
    }

    @Override
    public boolean hasNext()
    {
    	fail("no further element available");
        return false;
    }

    @Override
    public void init()
    {
    	fail("no init for FixedSourceTargetContinuousRouteTaskCreatorConfig");
    }
}
