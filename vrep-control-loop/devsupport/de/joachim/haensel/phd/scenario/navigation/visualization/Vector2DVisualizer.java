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
import de.joachim.haensel.streamextensions.IndexAdder;

public class Vector2DVisualizer extends JFrame
{
    public class ContentElememnt
    {
        private double[][] _content;
        private Color _color;

        public ContentElememnt(Deque<Vector2D> vectors, Color color)
        {
            if(_content == null || vectors.size() != _content.length)
            {
                _content = new double[vectors.size()][];
                initVectors(_content);
            }
            vectors.stream().map(IndexAdder.indexed()).forEachOrdered(v -> addInto(_content, v));
            _color = color;
        }
        
        private void addInto(double[][] content, IndexAdder<Vector2D> v)
        {
            int idx = v.idx();
            Vector2D vector = v.v();
            content[idx][0] = vector.getbX();
            content[idx][1] = vector.getbY();
            content[idx][2] = vector.getbX() + vector.getdX();
            content[idx][3] = vector.getbY() + vector.getdY();
        }
        
        private void initVectors(double[][] initializee)
        {
            for(int idx = 0; idx < initializee.length; idx++)
            {
                initializee[idx] = new double[4];
            }
        }
    }

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
        // holds information in the following way
        // first dimension is an ordered set of vectors to draw
        // second dimension is [baseX, baseY, tipX, tipY] of each vector
        // same as before but supposed to be drawn highlighted
        private ArrayList<ContentElememnt> _contentList;

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
            for (ContentElememnt content : _contentList)
            {
                double[][] transformedContent = transform(content._content, _zoomFactor, _xOffset, _yOffset);
                g2.setColor(content._color);
                Stroke s1 = new BasicStroke((float) 2.0);
                g2.setStroke(s1);
                Arrays.asList(transformedContent).stream().forEach(v -> drawVector(g2, v));
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

        private void drawVector(Graphics2D g2, double[] v)
        {
            double xB = v[0];
            double yB = v[1];
            double xT = v[2];
            double yT = v[3];
            g2.drawLine((int)xB, (int)yB, (int)xT, (int)yT);
            //tip. oval instead of arrow, cause it's easier
            double size = (ARROW_SIZE * _zoomFactor);
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
        
        public void addVectorSet(Deque<Vector2D> vectors, Color color)
        {
            _contentList.add(new ContentElememnt(vectors, color));
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
        _panel.addVectorSet(vectors, color);
    }
    
    public void updateVisuals()
    {
        this.repaint();
    }
}
