package de.joachim.haensel.phd.scenario.debug;

import java.awt.BorderLayout;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Speedometer extends JPanel
{
    private JLabel _speedLabel;
    private DecimalFormat _format;

    public static Speedometer createWindow()
    {
        JFrame frame = new JFrame("Speedometer");
        Speedometer instance = new Speedometer();
        instance.init();
        frame.add(instance);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return instance;
    }

    private void init()
    {
        setLayout(new BorderLayout());
        _format = new DecimalFormat("000.000");
        add(new JLabel("Shows current speed of car from controller"), BorderLayout.NORTH);
        _speedLabel = new JLabel(_format.format(0.0));
        add(_speedLabel, BorderLayout.SOUTH);
    }

    public void updateSpeed(float targetWheelRotation)
    {
        _speedLabel.setText(_format.format(targetWheelRotation));
        repaint();
    }
}
