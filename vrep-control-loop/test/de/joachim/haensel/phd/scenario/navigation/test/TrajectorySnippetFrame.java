package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;

public class TrajectorySnippetFrame extends JFrame
{
    private RoutePanel _trajectoryPanel;
    private JLabel _infoLabel;

    public TrajectorySnippetFrame()
    {
        initComponents();
    }

    private void initComponents()
    {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);
        setTitle("Zoomable Panel");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        _trajectoryPanel = new RoutePanel();

        _trajectoryPanel.setBounds(50, 50, width - 100, height - 240);
        _trajectoryPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(_trajectoryPanel);
        _trajectoryPanel.setVisible(true);

        _infoLabel = new JLabel("Roll to zoom. Click and drag to move.", JLabel.CENTER);
        _infoLabel.setFont(new Font(_infoLabel.getFont().getFontName(), Font.PLAIN, 26));
        _infoLabel.setBounds(50, height - 180, width - 100, 80);
        this.add(_infoLabel);
        _infoLabel.setVisible(true);
    }

    public void setCurRoute(List<Vector2D> snippet, Vector2D curVector)
    {
        _trajectoryPanel.setRouteAndVector(snippet, curVector);
    }
}
