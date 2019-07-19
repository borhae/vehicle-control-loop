package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.nodetypes.AngleNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.nodetypes.DisplacementNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.nodetypes.LeafNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.nodetypes.SetAngleNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.nodetypes.SetVelocityNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.nodetypes.VelocityNode;

public class OCStats
{
    private double _min;
    private double _max;
    private List<Double> _values;
    private String _type;

    public OCStats()
    {
        _values = new ArrayList<>();
    }
    
    public void visit(AngleNode node)
    {
        update(node.getNumericalValue());
        _type = node.getClass().getSimpleName();
    }

    public void visit(DisplacementNode node)
    {
        update(node.getNumericalValue());
        _type = node.getClass().getSimpleName();
    }

    public void visit(LeafNode node)
    {
        update(node.getNumericalValue());
        _type = node.getClass().getSimpleName();
    }

    public void visit(SetAngleNode node)
    {
        update(node.getNumericalValue());
        _type = node.getClass().getSimpleName();
    }

    public void visit(SetVelocityNode node)
    {
        update(node.getNumericalValue());
        _type = node.getClass().getSimpleName();
    }

    public void visit(VelocityNode node)
    {
        update(node.getNumericalValue());
        _type = node.getClass().getSimpleName();
    }
    
    private void update(double numericalValue)
    {
        if(numericalValue < _min)
        {
            _min = numericalValue;
        }
        if(numericalValue > _max)
        {
            _max = numericalValue;
        }
        _values.add(numericalValue);
    }

    @Override
    public String toString()
    {
        if(_values.isEmpty())
        {
            return "empty";
        }
        double average = _values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
        List<Double> sorted = _values.stream().mapToDouble(Double::doubleValue).sorted().boxed().collect(Collectors.toList());
        double median = 0.0;
        int middleIdx = sorted.size() / 2;
        if(sorted.size() % 2 == 0)
        {
            median = sorted.get(middleIdx) + sorted.get(middleIdx - 1) / 2.0;
        }
        else
        {
            median = sorted.get(middleIdx);
        }
        return String.format("Min: %.8f, Max: %.8f, average: %.8f, median: %.8f", _min, _max, average, median);
    }

    public String toNormyString()
    {
        if(_values.isEmpty())
        {
            return "empty";
        }
        if(_type.contains("Velocity"))
        {
            _values = _values.stream().map(vel -> UnitConverter.meterPerSecondToKilometerPerHour(vel)).collect(Collectors.toList());
            _min = UnitConverter.meterPerSecondToKilometerPerHour(_min);
            _max = UnitConverter.meterPerSecondToKilometerPerHour(_max);
        }
        if(_type.contains("Angle"))
        {
            _values = _values.stream().map(angle -> Math.toDegrees(angle)).collect(Collectors.toList());
            _min = Math.toDegrees(_min);
            _max = Math.toDegrees(_max);
        }
        double average = _values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
        List<Double> sorted = _values.stream().mapToDouble(Double::doubleValue).sorted().boxed().collect(Collectors.toList());
        double median = 0.0;
        int middleIdx = sorted.size() / 2;
        if(sorted.size() % 2 == 0)
        {
            median = sorted.get(middleIdx) + sorted.get(middleIdx - 1) / 2.0;
        }
        else
        {
            median = sorted.get(middleIdx);
        }
        return String.format("Type: %12s, Min: %.8f, Max: %.8f, average: %.8f, median: %.8f", _type.replace("Node", ""), _min, _max, average, median);
    }
}
