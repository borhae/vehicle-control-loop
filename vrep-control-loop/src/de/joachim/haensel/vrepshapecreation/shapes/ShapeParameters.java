package de.joachim.haensel.vrepshapecreation.shapes;

import java.util.List;

import de.joachim.haensel.vrepshapecreation.parameters.Parameters;

public class ShapeParameters extends Parameters
{
    public static final int GLOBAL_AND_LOCAL_RESPONDABLE_MASK = 0b1111_1111__1111_1111;
    public static final int GLOBAL_ONLY_RESPONDABLE_MASK = 0b1111_1111__0000_0000;

    private static final int CUT_UPPER_BITMASK = 0b0000_0000_0000_0000_1111_1111_1111_1111;
    private float _sizeX;
    private float _sizeY;
    private float _sizeZ;
    private float _mass;
    private EVRepShapes _type;
    private boolean _isRespondable;
    private boolean _isDynamic;
    private int _respondableMask;

    public ShapeParameters()
    {
        _sizeX = 0.0f;
        _sizeY = 0.0f;
        _sizeZ = 0.0f;
        _mass = 0.0f;
        _type = EVRepShapes.CONE;
        _isRespondable = false;
        _isDynamic = false;
        _respondableMask = 0b0;
    }
    
    @Override
    protected void populateInts(List<Integer> paramList)
    {
        paramList.add(_type.getValue());
        paramList.add(_isDynamic ? 1 : 0);
        paramList.add(_isRespondable ? 1 : 0);
        paramList.add(_respondableMask);
    }

    @Override
    protected void populateFloats(List<Float> paramList)
    {
        paramList.add(_sizeX);
        paramList.add(_sizeY);
        paramList.add(_sizeZ);
        paramList.add(_mass);
    }

    public void setSize(float sizeX, float sizeY, float sizeZ)
    {
        _sizeX = sizeX;
        _sizeY = sizeY;
        _sizeZ = sizeZ;
    }

    public void setMass(float mass)
    {
        _mass = mass;
    }

    public void setType(EVRepShapes type)
    {
        _type = type;
    }

    public void setIsRespondable(boolean isRespondable)
    {
        _isRespondable = isRespondable;
    }

    public void setIsDynamic(boolean isDynamic)
    {
        _isDynamic = isDynamic;
    }

    /**
     * Setting the local and global respondable bit mask
     * @param bitMask 0b xxxx xxxx gggg gggg llll llll with x - ignored, g - global and l - local
     */
    public void setRespondableMask(int bitMask)
    {
        _respondableMask = bitMask & CUT_UPPER_BITMASK;
    }
}
