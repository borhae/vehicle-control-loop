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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class Vector2DVisualizer extends JFrame
{
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
        private double _prevZoomFactor;
        private Map<Integer, ContentElement> _contentMap;

        public Vector2DVisualizerPanel()
        {
            _xOffset = 0.0;
            _yOffset = 0.0;
            _zoomFactor = 1.0;
            _prevZoomFactor = 1.0;
            _contentMap = new HashMap<>();
            addMouseWheelListener(this);
            addMouseMotionListener(this);
            addMouseListener(this);
            addKeyListener(this);
            setFocusable(true); //otherwise we get no typing
        }

        public Map<Integer, ContentElement> accessContentMap()
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
            Collection<ContentElement> contentList = accessContentMap().values();
            for (ContentElement content : contentList)
            {
                double[][] transformedContent = transform(content.getContent(), _zoomFactor, _xOffset, _yOffset);
                g2.setColor(content._color);
                Stroke strokeConfig = content._stroke == null ? new BasicStroke((float) 2.0) : content._stroke;
                g2.setStroke(strokeConfig);
                Arrays.asList(transformedContent).stream().forEach(v -> drawVector(g2, v, content.getTipSize(), compWidth, compHeight));
            }
        }

        private double[][] transform(double[][] content, double zoom, double xOffset, double yOffset)
        {
            double[][] transformedContent = new double[content.length][];
            for (int idx = 0; idx < content.length; idx++)
            {
                transformedContent[idx] = scale(content[idx], zoom);
            }
            for (int idx = 0; idx < content.length; idx++)
            {
                transformedContent[idx] = translate(transformedContent[idx], xOffset, yOffset);
            }
            return transformedContent;
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
            double[] result = new double[4];
            for (int idx = 0; idx < result.length; idx++)
            {
                result[idx] = v[idx] * zoomFactor;
            }
            return result;
        }

        private double[] translate(double[] v, double xOffset, double yOffset)
        {
            double[] result = new double[4];
            result[0] = (int)(v[0] + xOffset);
            result[1] = (int)(v[1] + yOffset);
            result[2] = (int)(v[2] + xOffset);
            result[3] = (int)(v[3] + yOffset);
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
            accessContentMap().put(nextID, new ContentElement(vectors, color, stroke));
            return nextID;
        }

        public Integer addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke, double tipSize)
        {
            Integer nextID = IDCreator.INSTANCE.getNextID();
            accessContentMap().put(nextID, new ContentElement(vectors, color, stroke, tipSize));
            return nextID;
        }

        public Integer addContentElement(ContentElement updateableContent)
        {
            Integer nextID = IDCreator.INSTANCE.getNextID();
            accessContentMap().put(nextID, updateableContent);
            return nextID;
        }

        public void updateContentElement(Integer id, Deque<Vector2D> vectors)
        {
            ContentElement contentElement = accessContentMap().get(id);
            contentElement.reset(vectors);
        }

        public void center()
        {
            double minX = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            Collection<ContentElement> contentElements = _contentMap.values();
            for (ContentElement curElem : contentElements)
            {
                double[][] content = curElem.getContent();
                for(int idx = 0; idx < content.length; idx++)
                {
                    double xB = content[idx][0];
                    double yB = content[idx][1];
                    double xT = content[idx][2];
                    double yT = content[idx][3];

                    minX = Math.min(Math.min(minX, xB), xT);
                    maxX = Math.max(Math.max(maxX, xB), xT);
                    
                    minY = Math.min(Math.min(minY, yB), yT);
                    maxY = Math.max(Math.max(maxY, yB), yT);
                }
            }
            double rangeX = maxX - minX;
            double frameWidth = (double)getWidth();
            double zoomX = frameWidth/rangeX;

            double rangeY = maxY - minY;
            double frameHeight = (double)getHeight();
            double zoomY = frameHeight/rangeY;
            
            _zoomFactor = Math.min(zoomX, zoomY);
            double centerX = (frameWidth - maxX * _zoomFactor) / 2.0;
            double centerY = (frameHeight - maxY * _zoomFactor) / 2.0;
            _xOffset = - minX * _zoomFactor + centerX;
            _yOffset = - minY * _zoomFactor + centerY;

            _prevZoomFactor = _zoomFactor;
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
    
    public int addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke)
    {
        return _panel.addVectorSet(vectors, color, stroke);
    }
    
    public int addContentElement(ContentElement updateableContent)
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
}
