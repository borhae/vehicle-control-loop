package de.joachim.haensel.phd.scenario.equivalenceclasses.visualization;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ArcsSegmentVisualizer extends JFrame
{
    
    public class ArcsSegmentsPanel extends JPanel
    {
        public ArcsSegmentsPanel()
        {
        }

        @Override
        public void paint(Graphics g)
        {
            super.paint(g);
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2 = (Graphics2D)g;
            g2.drawLine(50, 50, width - 50, height - 50); 
        }
    }

    private static final Dimension FRAME_SIZE = new Dimension(2560 / 2, 1440 / 2);
    private ArcsSegmentsPanel _panel;;

    public ArcsSegmentVisualizer()
    {
        setSize(FRAME_SIZE);
        setLayout(null);
        setTitle("Arcs and Segment Decomposition");
        _panel = new ArcsSegmentsPanel();
        int width = FRAME_SIZE.width;
        int height= FRAME_SIZE.height;
        int insets = 50;
        _panel.setBounds(insets, insets, width - insets, height - insets);
        this.add(_panel);
        _panel.setVisible(true);
    }

    
    
    public static void main(String[] args)
    {
        ArcsSegmentVisualizer vis = new ArcsSegmentVisualizer();
    }
}
