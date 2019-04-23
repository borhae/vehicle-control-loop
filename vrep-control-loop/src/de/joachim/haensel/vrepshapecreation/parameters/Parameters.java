package de.joachim.haensel.vrepshapecreation.parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.StringWA;

public abstract class Parameters
{
    protected float _x;
    protected float _y;
    protected float _z;
    protected float _beta;
    protected float _alpha;
    protected float _gamma;
    private String _name;

    public Parameters()
    {
        _x = 0.0f;
        _y = 0.0f;
        _z = 0.0f;
        _beta = 0.0f;
        _alpha = 0.0f;
        _gamma = 0.0f;
        _name = "unnamed";
    }
    
    @FunctionalInterface
    public interface LoopWithIndexAndSizeConsumer<T> 
    {
        void accept(T t, int i, int n);
    }

    public static <T> void forEachWithIdx(Collection<T> collection, LoopWithIndexAndSizeConsumer<T> consumer)
    {
       int index = 0;
       for (T object : collection)
       {
          consumer.accept(object, index++, collection.size());
       }
    }

    public void setPosition(float x, float y, float z)
    {
        _x = x; 
        _y = y;
        _z = z;
    }

    public void setOrientation(float alpha, float beta, float gamma)
    {
        _alpha = alpha;
        _beta = beta;
        _gamma = gamma;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public IntWA getInts()
    {
        List<Integer> paramList = new ArrayList<Integer>();
        populateIntsInternal(paramList);
    
        IntWA callParamsI = new IntWA(paramList.size());
        int[] intParams = callParamsI.getArray();
        forEachWithIdx(paramList, (cur, idx, cnt) -> intParams[idx] = cur.intValue());
        return callParamsI;
    }

    public FloatWA getFloats()
    {
        List<Float> paramList = new ArrayList<Float>();
        populateFloatsInternal(paramList);
        
        FloatWA callParamsF = new FloatWA(paramList.size());
        float[] floatparams = callParamsF.getArray();
        
        forEachWithIdx(paramList, (cur, idx, cnt) -> floatparams[idx] = cur.floatValue());
        
        return callParamsF;
    }

    private void populateIntsInternal(List<Integer> paramList)
    {
        populateInts(paramList);
    }

    protected abstract void populateInts(List<Integer> paramList);

    private void populateFloatsInternal(List<Float> paramList)
    {
        paramList.add(_x);
        paramList.add(_y);
        paramList.add(_z);
        paramList.add(_alpha);
        paramList.add(_beta);
        paramList.add(_gamma);
        populateFloats(paramList);
    }

    protected abstract void populateFloats(List<Float> paramList);

    public StringWA getStrings()
    {
        StringWA callParamsS = new StringWA(1);
        callParamsS.getArray()[0] = _name;
        
        return callParamsS;
    }
}