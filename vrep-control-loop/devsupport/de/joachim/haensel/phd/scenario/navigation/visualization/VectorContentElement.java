package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.streamextensions.IndexAdder;

public class VectorContentElement implements IContentElement
{
    // There are two dimensions maintained to work more quickly on the array when needed
    // holds information in the following way
    // first dimension is an ordered set of vectors to draw
    // second dimension is [baseX, baseY, tipX, tipY] of each vector
    private List<double[]> _content;
    private double[][] _arrayContent;
    private boolean _dirtyFlag;
    Color _color;
    Stroke _stroke;
    private double _tipSize;

    public VectorContentElement(Deque<Vector2D> vectors, Color color, Stroke stroke)
    {
        init(vectors, color, stroke);
        _tipSize = -1.0;
    }

    public VectorContentElement(Deque<Vector2D> vectors, Color color, Stroke stroke, double tipSize)
    {
        this(vectors, color, stroke);
        _tipSize = tipSize;
    }
    
    private void init(Deque<Vector2D> vectors, Color color, Stroke stroke)
    {
        _dirtyFlag = false;
        _arrayContent = null;
        if(_content == null || vectors.size() != _content.size())
        {
            _arrayContent = new double[vectors.size()][];
            initVectors(_arrayContent);
            _content = new ArrayList<>();
            vectors.stream().map(IndexAdder.indexed()).forEachOrdered(v -> addInto(_arrayContent, _content, v));
            _color = color;
            _stroke = stroke;
        }
    }
    
    public void addVector(Vector2D v)
    {
        _content.add(new double[]{v.getbX(), v.getbY(), v.getbX() + v.getdX(), v.getbY() + v.getdY()});
        _dirtyFlag = true;
    }
    
    private void addInto(double[][] aContent, List<double[]> lContent, IndexAdder<Vector2D> v)
    {
        int idx = v.idx();
        Vector2D vector = v.v();
        aContent[idx][0] = vector.getbX();
        aContent[idx][1] = vector.getbY();
        aContent[idx][2] = vector.getbX() + vector.getdX();
        aContent[idx][3] = vector.getbY() + vector.getdY();
        lContent.add(aContent[idx]);
    }
    
    private void initVectors(double[][] initializee)
    {
        for(int idx = 0; idx < initializee.length; idx++)
        {
            initializee[idx] = new double[4];
        }
    }

    public double getTipSize()
    {
        return _tipSize;
    }

    public double[][] getContent()
    {
        if(_dirtyFlag)
        {
            double[][] result = _content.toArray(new double[0][0]);
            _arrayContent = result;
            return result;
        }
        else
        {
            return _arrayContent;
        }
    }

    public void reset(Deque<Vector2D> vectors)
    {
        _content = null;
        init(vectors, _color, _stroke);
    }

    @Override
    public VisualizerContentType getType()
    {
        return VisualizerContentType.VECTOR;
    }

    @Override
    public Color getColor()
    {
        return _color;
    }

    @Override
    public Stroke getStroke()
    {
        return _stroke;
    }
}