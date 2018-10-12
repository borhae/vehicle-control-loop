package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Arc;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Segment;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.streamextensions.IndexAdder;

public class ArcSegmentContentElement implements IContentElement
{
    public static final double ARC = 0;
    public static final double SEGMENT = 1;
    // There are a list and an array maintained to work more quickly on the array when needed
    // The array holds information in the following way
    // first dimension is an ordered set of elements to draw
    // second dimension is either [x1,  y1,  x2,  y2,  0,   0,   SEGMENT] if it is a segment
    // or                         [c_x, c_y, x_s, y_s, x_e, y_e, ARC    ] if it is an arc
    private List<double[]> _content;
    private double[][] _arrayContent;
    private boolean _dirtyFlag;
    Color _color;
    Stroke _stroke;
    private double _tipSize;

    public ArcSegmentContentElement(List<IArcsSegmentContainerElement> segments, Color color, Stroke stroke)
    {
        init(segments, color, stroke);
    }

    @Override
    public VisualizerContentType getType()
    {
        return VisualizerContentType.ARC_SEGMENT;
    }

    private void init(List<IArcsSegmentContainerElement> segments, Color color, Stroke stroke)
    {
        _dirtyFlag = false;
        _arrayContent = null;
        if(_content == null || segments.size() != _content.size())
        {
            _arrayContent = new double[segments.size()][];
            initElements(_arrayContent);
            _content = new ArrayList<>();
            segments.stream().map(IndexAdder.indexed()).forEachOrdered(v -> addInto(_arrayContent, _content, v));
            _color = color;
            _stroke = stroke;
        }
    }
    
    private void addInto(double[][] aContent, List<double[]> lContent, IndexAdder<IArcsSegmentContainerElement> v)
    {
        int idx = v.idx();
        IArcsSegmentContainerElement element = v.v();
        if(element instanceof Arc)
        {
            Arc arc = (Arc) element;
            aContent[idx][0] = arc.getCenter().getX();
            aContent[idx][1] = arc.getCenter().getY();
            aContent[idx][2] = arc.getStart().getX();
            aContent[idx][3] = arc.getStart().getY();
            aContent[idx][4] = arc.getEnd().getX();
            aContent[idx][5] = arc.getEnd().getY();
            aContent[idx][6] = ARC; 
        }
        else if(element instanceof Segment)
        {
            Line2D line = ((Segment) element).getLine();
            aContent[idx][0] = line.getX1();
            aContent[idx][1] = line.getY1();
            aContent[idx][2] = line.getX2();
            aContent[idx][3] = line.getY2();
            aContent[idx][4] = 0.0;
            aContent[idx][5] = 0.0;
            aContent[idx][6] = SEGMENT; 
        }
        lContent.add(aContent[idx]);
    }
    
    private void initElements(double[][] initializee)
    {
        for(int idx = 0; idx < initializee.length; idx++)
        {
            initializee[idx] = new double[7];
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
