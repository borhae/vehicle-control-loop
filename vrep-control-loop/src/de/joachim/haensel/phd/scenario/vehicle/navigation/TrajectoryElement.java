package de.joachim.haensel.phd.scenario.vehicle.navigation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class TrajectoryElement implements Comparable<TrajectoryElement>
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
    private boolean _reverse;

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
        _reverse = false;
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
    
    public boolean isReverse()
    {
        return _reverse;
    }

    public void setReverse(boolean reverse)
    {
        _reverse = reverse;
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

    @Override
    public int compareTo(TrajectoryElement other)
    {
        int velComp = Double.compare(_velocity, other.getVelocity());
        if(velComp == 0)
        {
            Vector2D otherVec = other.getVector();
            Position2D otherBase = otherVec.getBase();
            Position2D thisBase = _vector.getBase();
            int basXComp = Double.compare(thisBase.getX(), otherBase.getX());
            if(basXComp == 0)
            {
                int basYComp = Double.compare(thisBase.getY(), otherBase.getY());
                if(basYComp == 0)
                {
                    Position2D thisTip = _vector.getTip();
                    Position2D otherTip = otherVec.getTip();
                    int tipXComp = Double.compare(thisTip.getX(), otherTip.getX());
                    if(tipXComp == 0)
                    {
                        int tipYComp = Double.compare(thisTip.getY(), otherTip.getY());
                        return tipYComp;
                    }
                    else
                    {
                        return tipXComp;
                    }
                }
                else
                {
                    return basYComp;
                }
            }
            else
            {
                return basXComp;
            }
        }
        else
        {
            return velComp;
        }
    }

    public void setVector(Vector2D v)
    {
        _vector = v;
    }
}
