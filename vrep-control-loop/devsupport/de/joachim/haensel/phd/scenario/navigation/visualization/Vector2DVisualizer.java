package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class Vector2DVisualizer extends JFrame
{
    public class Extrema
    {
        double _minX = Double.POSITIVE_INFINITY;
        double _minY = Double.POSITIVE_INFINITY;
        double _maxX = Double.NEGATIVE_INFINITY;
        double _maxY = Double.NEGATIVE_INFINITY;
    }
    
    public enum IDCreator
    {
        INSTANCE;
        
        private Integer _counter = Integer.valueOf(0);
        
        public Integer getNextID()
        {
            Integer next = Integer.valueOf(_counter.intValue() + 1);
            _counter = next;
            return _counter;
        }
    }

    private static final Dimension FRAME_SIZE = new Dimension(2560, 1440);

    public class Vector2DVisualizerPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, KeyListener
    {

        private static final double ARROW_SIZE = 5.0;
        private boolean _zoomer;
        private double _zoomFactor;
        private Point _startPoint;
        private int _xDiff;
        private int _yDiff;
        private boolean _dragger;
        private boolean _released;
        private double _xOffset;
        private double _yOffset;
        private double _yScale;
        private double _xScale;
        private double _prevZoomFactor;
        private Map<Integer, IContentElement> _contentMap;

        public Vector2DVisualizerPanel()
        {
            _xOffset = 0.0;
            _yOffset = 0.0;
            _xScale = 1.0;
            _yScale = 1.0;
            _zoomFactor = 1.0;
            _prevZoomFactor = 1.0;
            _contentMap = new HashMap<>();
            addMouseWheelListener(this);
            addMouseMotionListener(this);
            addMouseListener(this);
            addKeyListener(this);
            setFocusable(true); //otherwise we get no typing
        }

        public Map<Integer, IContentElement> accessContentMap()
        {
            return _contentMap;
        }
        
        /**
         * Switched Y-axis because almost all of the input is y-up
         */
        @Override
        public void paint(Graphics g)
        {
            super.paint(g);
            int compWidth = getWidth();
            int compHeight = getHeight();
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (_zoomer)
            {
                double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
                double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

                double zoomDiv = _zoomFactor / _prevZoomFactor;

                _xOffset = (zoomDiv) * (_xOffset) + (1 - zoomDiv) * xRel;
                _yOffset = (zoomDiv) * (_yOffset) + (1 - zoomDiv) * yRel;
                _prevZoomFactor = _zoomFactor;
                _zoomer = false;
            }
            else if (_dragger)
            {
                if (_released)
                {
                    _xOffset += _xDiff;
                    _yOffset -= _yDiff;
                    _dragger = false;
                }
            }
            // draw stuff here
            Collection<IContentElement> contentList = accessContentMap().values();
            for (IContentElement content : contentList)
            {
                drawContentElement(content, g2, compWidth, compHeight);
            }
        }

        private void drawContentElement(IContentElement content, Graphics2D g2, int compWidth, int compHeight)
        {
            double[][] transformedContent = transform(content.getContent(), _zoomFactor, _xOffset, _yOffset, _xScale, _yScale);
            g2.setColor(content.getColor());
            Stroke strokeConfig = content.getStroke() == null ? new BasicStroke((float) 2.0) : content.getStroke();
            g2.setStroke(strokeConfig);
            Consumer<? super double[]> drawAction = v -> System.out.println("didn't find a type for this: " + v);
            if(content.getType() == VisualizerContentType.VECTOR)
            {
                VectorContentElement vectorContent = (VectorContentElement) content;
                drawAction = v -> drawVector(g2, v, vectorContent.getTipSize(), compWidth, compHeight);
            }
            else if(content.getType() == VisualizerContentType.ARC_SEGMENT)
            {
                drawAction = v -> drawArcSegment(g2, v, compWidth, compHeight);
            }
            Arrays.asList(transformedContent).stream().forEach(drawAction);
        }

        private double[][] transform(double[][] content, double zoom, double xOffset, double yOffset, double xScale, double yScale)
        {
            double[][] transformedContent = new double[content.length][];
            for (int idx = 0; idx < content.length; idx++)
            {
                transformedContent[idx] = scale(content[idx], zoom);
            }
            //TODO ignore for now since it doesn't work anyway
//            for (int idx = 0; idx < content.length; idx++)
//            {
//                transformedContent[idx] = scaleXY(transformedContent[idx], xScale, yScale);
//            }
            for (int idx = 0; idx < content.length; idx++)
            {
                transformedContent[idx] = translate(transformedContent[idx], xOffset, yOffset);
            }
            return transformedContent;
        }
        
        private String toStringC(double[][] content)
        {
            String string = "";
            for (int idx = 0; idx < content.length; idx++)
            {
                double[] curElem = content[idx];
                string = string + "[";
                for (int idxInner = 0; idxInner < curElem.length; idxInner++)
                {
                    string = string + ", " + curElem[idxInner];
                }
                string = string + "]";
            }
            return string;
        }

        private void drawArcSegment(Graphics2D g2, double[] v, int compWidth, int compHeight)
        {
            if (v[6] == ArcSegmentContentElement.ARC)
            {
                double c_x = v[0];
                double c_y = v[1];
                double s_x = v[2];
                double s_y = v[3];
                double e_x = v[4];
                double e_y = v[5];
                Position2D start = new Position2D(s_x, compHeight - s_y);
                Position2D end = new Position2D(e_x, compHeight - e_y);
                Position2D center = new Position2D(c_x, compHeight - c_y);
                double radius = Position2D.distance(start, center);

//                drawArcAWT(g2, compHeight, c_x, c_y, start, end, center, radius);
                drawArc(g2, compHeight, c_x, c_y, start, end, center, radius);
            }
            else if (v[6] == ArcSegmentContentElement.SEGMENT)
            {
                double xB = v[0];
                double yB = v[1];
                double xT = v[2];
                double yT = v[3];
                g2.drawLine((int) xB, compHeight - (int) yB, (int) xT, compHeight - (int) yT);
            }
        }

        private void drawArc(Graphics2D g2, int compHeight, double c_x, double c_y, Position2D start, Position2D end, Position2D center, double radius)
        {
            int x = (int)(center.getX() - radius);
            int y = (int)(center.getY() - radius);
            int width = (int)(radius * 2);
            int height = (int)(radius * 2);
            int startAngle = (int)(180 / Math.PI * Math.atan2(start.getY() - center.getY(), start.getX() - center.getX()));
            int endAngle = (int)(180 / Math.PI * Math.atan2(end.getY() - center.getY(), end.getX() - center.getX()));
            g2.drawArc(x, compHeight - y, width, height, startAngle, endAngle);
        }

        private void drawArcAWT(Graphics2D g2, int compHeight, double c_x, double c_y, Position2D start, Position2D end,
                Position2D center, double radius)
        {
            Position2D a = Position2D.minus(start, center);
            Position2D b = Position2D.minus(end, center);
            Position2D c = new Position2D(radius, 0.0); //0 degree
//                double angle1 = Math.atan2(a.getY() - c.getY(), a.getX() - c.getX());
//                double angle2 = Math.atan2(b.getY() - c.getY(), b.getX() - c.getX())
//                double angle1 = Math.atan2(c.getY() - a.getY(), c.getX() - a.getX());
//                double angle2 = Math.atan2(c.getY() - b.getY(), c.getX() - b.getX());
            double angle1 = -Vector2D.computeAngle(new Vector2D(center, start), new Vector2D(center, new Position2D(10.0, center.getY())));
            double angle2 = -Vector2D.computeAngle(new Vector2D(center, end), new Vector2D(center, new Position2D(10.0, center.getY())));

            double angle1Deg = Math.toDegrees(angle1);
            double angle2Deg = Math.toDegrees(angle2);
            
            int x = (int)(c_x - radius);
            int y = (int)(c_y - radius);
            int width = (int)radius * 2;
            int height = (int) radius * 2;
            int startAngle = (int)Math.toDegrees(angle1);
            int arcAngle = (int)Math.toDegrees(angle2 - angle1);
            // draw an arc
            g2.drawArc(x, compHeight - y - height, width, height, (int)(startAngle + 180), arcAngle);
            g2.setColor(Color.RED);
            g2.drawOval((int)start.getX() - 5, (int)(start.getY() - 5), 10, 10);
            g2.setColor(Color.BLUE);
            g2.drawOval((int)end.getX() - 5, (int)(end.getY() - 5), 10, 10);
            g2.setColor(Color.ORANGE);
            g2.drawOval((int)center.getX() - 5, (int)(center.getY() - 5), 10, 10);
            g2.drawString("hello", x, compHeight - y);
            g2.setColor(Color.BLACK);
        }

        private void drawVector(Graphics2D g2, double[] v, double tipSize, int compWidth, int compHeight)
        {
            double xB = v[0];
            double yB = v[1];
            double xT = v[2];
            double yT = v[3];
            g2.drawLine((int)xB, compHeight - (int)yB, (int)xT, compHeight - (int)yT);
            drawArrowTip(g2, tipSize, xB, compHeight - yB, xT, compHeight - yT);
        }

        private void drawArrowTip(Graphics2D g2, double tipSize, double xB, double yB, double xT, double yT)
        {
            if((xB == xT) && (yB == yT))
            {
//                if there is no direction, there is no way we can point this anywhere 
                drawCircleTip(g2, tipSize, xB, yB, xT, yT);
            }
            else
            {
                Position2D norm = new Position2D(xT - xB, yT - yB);
                norm.normalize();
                
                double size = (ARROW_SIZE * _zoomFactor);
                if(tipSize > 0.0)
                {
                    size *= tipSize;
                }
                
                Position2D leftWing = norm.copy().transform(new TMatrix(size, 0.0, 0.0, 1.25 * Math.PI));
                Position2D rightWing = norm.copy().transform(new TMatrix(size, 0.0, 0.0, 0.75 * Math.PI));
                g2.drawLine((int)xT, (int)yT, (int)(xT + leftWing.getX()), (int)(yT + leftWing.getY()));
                g2.drawLine((int)xT, (int)yT, (int)(xT + rightWing.getX()), (int)(yT + rightWing.getY()));
            }
        }

        private void drawCircleTip(Graphics2D g2, double tipSize, double xB, double yB, double xT, double yT)
        {
            //tip. oval instead of arrow, cause it's easier
            double size = (ARROW_SIZE * _zoomFactor);
            if(tipSize > 0.0)
            {
                size *= tipSize;
            }
            double dx = xT - xB;
            double dy = yT - yB;
            
            double delX = size/2.0;
            double delY = size/2.0;
            
            if( (dx != 0.0) || (dy != 0.0) )
            {
                double l = Math.sqrt(dx * dx + dy * dy);
                double nx = dx / l;
                double ny = dy / l;
                delX = size/2.0 * (1.0 + nx);
                delY = size/2.0 * (1.0 + ny);
            }
            double x = xT - delX;
            double y = yT - delY;
            g2.drawOval((int)x, (int)y, (int)size, (int)size);
        }

        private double[] scale(double[] v, double zoomFactor)
        {
            //TODO some day I'll do this properly
            double[] result = new double[v.length];
            for (int idx = 0; idx < result.length; idx++)
            {
                //6th index carries type information so keep it like it is (I know this is superbad)
                result[idx] = idx == 6 ? v[idx] : v[idx] * zoomFactor;
            }
            return result;
        }

        private double[] translate(double[] v, double xOffset, double yOffset)
        {
            double[] result = new double[v.length];
            for(int idx = 0; idx < result.length - 1; idx+=2)
            {
                result[idx] = idx == 6 ? v[idx] : (int)(v[idx] + xOffset);
                result[idx + 1] = idx == 6 ? v[idx] : (int)(v[idx + 1] + yOffset);
            }
            if(v.length == 7)
            {
                result[6] = v[6];
            }
            return result;
        }
        
        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            _zoomer = true;
            double zoomDelta = 0.0;
            if(_zoomFactor <= 10)
            {
                zoomDelta = 1.1; //this was the default
            }
            else if(_zoomFactor <= 50)
            {
                zoomDelta = 1.01;
            }
            else if(_zoomFactor <= 200)
            {
                zoomDelta = 1.005;
            }
            else 
            {
                zoomDelta = 1.001;
            }
            if (e.getWheelRotation() < 0)
            {
                _zoomFactor *= zoomDelta;
                repaint();
            }
            else if (e.getWheelRotation() > 0)
            {
                _zoomFactor /= zoomDelta;
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            Point curPoint = e.getLocationOnScreen();
            _xDiff = curPoint.x - _startPoint.x;
            _yDiff = curPoint.y - _startPoint.y;

            _dragger = true;
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            _released = false;
            _startPoint = MouseInfo.getPointerInfo().getLocation();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            _released = true;
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }
        
        public Integer addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke)
        {
            
            Integer nextID = IDCreator.INSTANCE.getNextID();
            accessContentMap().put(nextID, new VectorContentElement(vectors, color, stroke));
            return nextID;
        }

        public Integer addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke, double tipSize)
        {
            Integer nextID = IDCreator.INSTANCE.getNextID();
            accessContentMap().put(nextID, new VectorContentElement(vectors, color, stroke, tipSize));
            return nextID;
        }

        public int addArcsSegments(List<IArcsSegmentContainerElement> segments, Color color, Stroke stroke)
        {
            Integer nexID = IDCreator.INSTANCE.getNextID();
            accessContentMap().put(nexID, new ArcSegmentContentElement(segments, color, stroke));
            return nexID;
        }

        public Integer addContentElement(IContentElement updateableContent)
        {
            Integer nextID = IDCreator.INSTANCE.getNextID();
            accessContentMap().put(nextID, updateableContent);
            return nextID;
        }

        public void updateContentElement(Integer id, Deque<Vector2D> vectors)
        {
            VectorContentElement contentElement = (VectorContentElement)accessContentMap().get(id);
            contentElement.reset(vectors);
        }

        public void center()
        {
            _xScale = 1.0;
            _yScale = 1.0;
            Extrema extrema = new Extrema();
            Collection<IContentElement> contentElements = _contentMap.values();
            for (IContentElement curElem : contentElements)
            {
                switch (curElem.getType())
                {
                    case VECTOR:
                        computeExtremaForVectors(extrema, curElem);
                        break;
                    case ARC_SEGMENT:
                        computeExtremaForArcSegments(extrema, curElem);
                        break;
                    default:
                        break;
                }
            }
            double rangeX = extrema._maxX - extrema._minX;
            double frameWidth = (double)getWidth();
            double zoomX = frameWidth/rangeX;

            double rangeY = extrema._maxY - extrema._minY;
            double frameHeight = (double)getHeight();
            double zoomY = frameHeight/rangeY;
            
            _zoomFactor = Math.min(zoomX, zoomY);
            double centerX = (frameWidth - extrema._maxX * _zoomFactor) / 2.0;
            double centerY = (frameHeight - extrema._maxY * _zoomFactor) / 2.0;
            _xOffset = - extrema._minX * _zoomFactor + centerX;
            _yOffset = - extrema._minY * _zoomFactor + centerY;
            
            _prevZoomFactor = _zoomFactor;
        }

        private void computeExtremaForArcSegments(Extrema extrema, IContentElement curElem)
        {
            double[][] content = curElem.getContent();
            for(int idx = 0; idx < content.length; idx++)
            {
                double[] curArcOrSegment = content[idx];
                if(curArcOrSegment[6] == ArcSegmentContentElement.ARC)
                {
                    //TODO this actually just creates a bounding box for the circle. the arc box will follow and is too much work right now
                    double c_x = curArcOrSegment[0];
                    double c_y = curArcOrSegment[1];
                    double s_x = curArcOrSegment[2];
                    double s_y = curArcOrSegment[3];
                    double radius = Position2D.distance(c_x, c_y, s_x, s_y);

                    int x = (int)(c_x - radius);
                    int y = (int)(c_y - radius);
                    int width = (int)radius * 2;
                    int height = (int) radius * 2;
                    extrema._minX = Math.min(extrema._minX, x);
                    extrema._maxX = Math.max(extrema._maxX, x + width);
                    
                    extrema._minY = Math.min(extrema._minY, y);
                    extrema._maxY = Math.max(extrema._maxY, y + height);
                }
                else if(curArcOrSegment[6] == ArcSegmentContentElement.SEGMENT)
                {
                    double xB = content[idx][0];
                    double yB = content[idx][1];
                    double xT = content[idx][2];
                    double yT = content[idx][3];
                    
                    extrema._minX = Math.min(Math.min(extrema._minX, xB), xT);
                    extrema._maxX = Math.max(Math.max(extrema._maxX, xB), xT);
                    
                    extrema._minY = Math.min(Math.min(extrema._minY, yB), yT);
                    extrema._maxY = Math.max(Math.max(extrema._maxY, yB), yT);
                }
            }
        }

        private void computeExtremaForVectors(Extrema extrema, IContentElement curUntypedElem)
        {
            VectorContentElement curElem = (VectorContentElement)curUntypedElem;
            double[][] content = curElem.getContent();
            for(int idx = 0; idx < content.length; idx++)
            {
                double xB = content[idx][0];
                double yB = content[idx][1];
                double xT = content[idx][2];
                double yT = content[idx][3];

                extrema._minX = Math.min(Math.min(extrema._minX, xB), xT);
                extrema._maxX = Math.max(Math.max(extrema._maxX, xB), xT);
                
                extrema._minY = Math.min(Math.min(extrema._minY, yB), yT);
                extrema._maxY = Math.max(Math.max(extrema._maxY, yB), yT);
            }
        }

        @Override
        public void keyTyped(KeyEvent e)
        {
            System.out.println("key typed: " + e.getKeyChar());
            char keyWhenReleased = e.getKeyChar();
            if(keyWhenReleased == 'c')
            {
                center();
                repaint();
            }
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
        }

        /**
         * doesn't work yet. Is supposed to automatically scale so everything is fitted to it's max
         */
        public void autoFit()
        {
            Extrema extrema = new Extrema();
            Collection<IContentElement> contentElements = _contentMap.values();
            for (IContentElement curElem : contentElements)
            {
                switch (curElem.getType())
                {
                    case VECTOR:
                        computeExtremaForVectors(extrema, curElem);
                        break;
                    default:
                        break;
                }
            }
            double rangeX = extrema._maxX - extrema._minX;
            double frameWidth = (double)getWidth();

            double rangeY = extrema._maxY - extrema._minY;
            double frameHeight = (double)getHeight();

            _xScale = frameWidth/rangeX;
            _yScale = frameHeight/rangeY;
        }
    }

    private Vector2DVisualizerPanel _panel;
    private JLabel _infoLabel;

    public Vector2DVisualizer()
    {
        setSize(FRAME_SIZE);
        setLayout(null);
        setTitle("Vector Visualization");

        int width = FRAME_SIZE.width;
        int height = FRAME_SIZE.height;
        
        _panel = new Vector2DVisualizerPanel();

        _panel.setBounds(50, 50, width - 100, height - 240);
        _panel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(_panel);
        _panel.setVisible(true);

        _infoLabel = new JLabel("Roll to zoom. Click and drag to move. Press c to center", JLabel.CENTER);
        _infoLabel.setFont(new Font(_infoLabel.getFont().getFontName(), Font.PLAIN, 26));
        _infoLabel.setBounds(50, height - 180, width - 100, 80);
        this.add(_infoLabel);
        _infoLabel.setVisible(true);
    }

    public void showOnScreen(int screen)
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if (screen > -1 && screen < gd.length)
        {
            this.setLocation(gd[screen].getDefaultConfiguration().getBounds().x, this.getY());
        }
        else if (gd.length > 0)
        {
            this.setLocation(gd[0].getDefaultConfiguration().getBounds().x, this.getY());
        }
        else
        {
            throw new RuntimeException("No Screens Found");
        }
    }
    
    public int addVectorSet(Deque<Vector2D> vectors, Color color)
    {
        return _panel.addVectorSet(vectors, color, null);
    }
    
    public int addVectorSet(Deque<Vector2D> vectors, Color color, double width, double tipSize)
    {
        Stroke stroke = new BasicStroke((float)width);
        return _panel.addVectorSet(vectors, color, stroke, tipSize);
    }
    
    public int addArcsSegments(List<IArcsSegmentContainerElement> segments, Color color, double width)
    {
        Stroke stroke = new BasicStroke((float)width);
        return _panel.addArcsSegments(segments, color, stroke);
    }
    
    public int addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke)
    {
        return _panel.addVectorSet(vectors, color, stroke);
    }
    
    public int addContentElement(IContentElement updateableContent)
    {
        return _panel.addContentElement(updateableContent);
    }
    
    public void updateContentElement(int id, Deque<Vector2D> vectors)
    {
        _panel.updateContentElement(id, vectors);
    }
    
    public void updateVisuals()
    {
        this.repaint();
    }

    public void centerContent()
    {
        _panel.center();
        this.repaint();
    }

    public void autoFit()
    {
        _panel.autoFit();
        this.repaint();
    }
}
