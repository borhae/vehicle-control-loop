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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;

public class Vector2DVisualizer extends JFrame
{
    private static final Dimension FRAME_SIZE = new Dimension(2560, 1440);

    public class Vector2DVisualizerPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener
    {

        private static final double ARROW_SIZE = 1.0;
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
        private ArrayList<ContentElement> _contentList;

        public Vector2DVisualizerPanel()
        {
            _xOffset = 0.0;
            _yOffset = 0.0;
            _zoomFactor = 1.0;
            _prevZoomFactor = 1.0;
            _contentList = new ArrayList<>();
            addMouseWheelListener(this);
            addMouseMotionListener(this);
            addMouseListener(this);
        }

        @Override
        public void paint(Graphics g)
        {
            super.paint(g);
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
                    _yOffset += _yDiff;
                    _dragger = false;
                }
            }
            
            // draw stuff here
            for (ContentElement content : _contentList)
            {
                double[][] transformedContent = transform(content.getContent(), _zoomFactor, _xOffset, _yOffset);
                g2.setColor(content._color);
                Stroke strokeConfig = content._stroke == null ? new BasicStroke((float) 2.0) : content._stroke;
                g2.setStroke(strokeConfig);
                Arrays.asList(transformedContent).stream().forEach(v -> drawVector(g2, v, content.getTipSize()));
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

        private void drawVector(Graphics2D g2, double[] v, double tipSize)
        {
            double xB = v[0];
            double yB = v[1];
            double xT = v[2];
            double yT = v[3];
            g2.drawLine((int)xB, (int)yB, (int)xT, (int)yT);
            drawCircleTip(g2, tipSize, xB, yB, xT, yT);
//            drawArrowTip(g2, tipSize, xB, yB, xT, yT);
        }

//        private void drawArrowTip(Graphics2D g2, double tipSize, double xB, double yB, double xT, double yT)
//        {
//            double angle = Math.atan2(yT - yB, xT - xB);
//            Vector2D v1 = new Vector2D(0, 0, )
//        }

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
            double l = Math.sqrt(dx * dx + dy * dy);
            double nx = dx / l;
            double ny = dy / l;
            double delX = size/2.0 * (1.0 + nx);
            double delY = size/2.0 * (1.0 + ny);
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

            if (e.getWheelRotation() < 0)
            {
                _zoomFactor *= 1.1;
                repaint();
            }
            else if (e.getWheelRotation() > 0)
            {
                _zoomFactor /= 1.1;
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
        
        public void addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke)
        {
            _contentList.add(new ContentElement(vectors, color, stroke));
        }

        public void addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke, double tipSize)
        {
            _contentList.add(new ContentElement(vectors, color, stroke, tipSize));
        }

        public void addContentElement(ContentElement updateableContent)
        {
            _contentList.add(updateableContent);
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

        _infoLabel = new JLabel("Roll to zoom. Click and drag to move.", JLabel.CENTER);
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
    
    public void addVectorSet(Deque<Vector2D> vectors, Color color)
    {
        _panel.addVectorSet(vectors, color, null);
    }
    
    public void addVectorSet(Deque<Vector2D> vectors, Color color, double width, double tipSize)
    {
        Stroke stroke = new BasicStroke((float)width);
        _panel.addVectorSet(vectors, color, stroke, tipSize);
    }
    
    public void addVectorSet(Deque<Vector2D> vectors, Color color, Stroke stroke)
    {
        _panel.addVectorSet(vectors, color, stroke);
    }
    
    public void addContentElement(ContentElement updateableContent)
    {
        _panel.addContentElement(updateableContent);
    }
    
    public void updateVisuals()
    {
        this.repaint();
    }
}
