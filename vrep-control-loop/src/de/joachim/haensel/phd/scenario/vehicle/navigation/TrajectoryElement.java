package de.joachim.haensel.phd.scenario.vehicle.navigation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class TrajectoryElement
{
    public enum VelocityEdgeType
    {
        START, RAISE, FALL, UNKNOWN
    }

    private Vector2D _vector;
//TODO remove the different types
    private TrajectoryType _type;
    private double _velocity;
    private VelocityEdgeType _velocityEdgeType;
    private double _radius;
    private double _kappa;
    private int _index;

    public TrajectoryElement()
    {
    }
    
    public TrajectoryElement(Vector2D vector)
    {
        _vector = vector;
        _type = TrajectoryType.UNKNOWN;
        _velocity = 0.0;
        _velocityEdgeType = VelocityEdgeType.UNKNOWN;
        _radius = 0.0;
        _kappa = 0.0;
    }

    public Vector2D getVector()
    {
        return _vector;
    }

    @Override
    public String toString()
    {
        return _vector.toString();
    }

    public void setIsOverlay()
    {
        _type = TrajectoryType.OVERLAY;
    }

    public void setIsOriginal()
    {
        _type = TrajectoryType.ORIGINAL;
    }

    public boolean hasType(TrajectoryType other)
    {
        return _type == other;
    }

    public void setVelocity(double velocity)
    {
        _velocity = velocity;
    }

    public String toCSV()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(_velocity);
        return sb.toString();
    }

    public double getVelocity()
    {
        return _velocity;
    }

    public void setRiseFall(VelocityEdgeType type)
    {
        _velocityEdgeType = type;
    }

    public VelocityEdgeType getRiseFall()
    {
        return _velocityEdgeType;
    }

    public void setRadius(double radius)
    {
        _radius = radius;
    }

    public void setKappa(double kappa)
    {
        _kappa = kappa;
    }

    public double getRadius()
    {
        return _radius;
    }

    public double getKappa()
    {
        return _kappa;
    }

    public void setIdx(int index)
    {
        _index = index;
    }

    public int getIdx()
    {
        return _index;
    }

    public TrajectoryElement transform(TMatrix transformationMatrix)
    {
        _vector.transform(transformationMatrix);
        return this;
    }

    public TrajectoryElement deepCopy()
    {
        Vector2D vCopy = new Vector2D(_vector);
        TrajectoryElement copy = new TrajectoryElement(vCopy);
        copy._index = _index;
        copy._kappa = _kappa;
        copy._radius = _radius;
        copy._type = _type;
        copy._velocity = _velocity;
        copy._velocityEdgeType = _velocityEdgeType;
        return copy;
    }
}
