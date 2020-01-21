package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.NamedObsConf;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class MongoTrajectory
{
    private Integer _idx;
    private List<List<Double>> _trajectory;
    private String _name;

    public MongoTrajectory()
    {
    }
    
    public MongoTrajectory(NamedObsConf namedObsConf, Integer idx)
    {
        _trajectory = trajectoryToDoubleArrayTrajectory(namedObsConf.getConfiguration());
        _idx = idx;
        _name = namedObsConf.getName();
    }

    public Integer getIdx()
    {
        return _idx;
    }

    public void setIdx(Integer idx)
    {
        _idx = idx;
    }

    public List<List<Double>> getTrajectory()
    {
        return _trajectory;
    }

    public void setTrajectory(List<List<Double>> trajectory)
    {
        _trajectory = trajectory;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    private List<List<Double>> trajectoryToDoubleArrayTrajectory(List<TrajectoryElement> trajectory)
    {
        Function<TrajectoryElement, List<Double>> toList = 
                t -> {Vector2D v = t.getVector(); List<Double> result = Arrays.asList(v.getbX(), v.getbY(), t.getVelocity()); return result;};
        List<List<Double>> result = trajectory.stream().map(toList).collect(Collectors.toList());
        
        TrajectoryElement t = trajectory.get(trajectory.size() - 1);
        Position2D tTip = t.getVector().getTip();
        List<Double> last = Arrays.asList(tTip.getX(), tTip.getY(), t.getVelocity());
        result.add(last);
        return result;
    }
}
