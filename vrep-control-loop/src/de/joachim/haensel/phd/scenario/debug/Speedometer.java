package de.joachim.haensel.phd.scenario.debug;

import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class Speedometer extends JPanel
{
    private JLabel _wheelSpeedLabel;
    private DecimalFormat _format;
    private JLabel _segmentLabel;
    private JLabel _velocityLabel;

    public static Speedometer createWindow()
    {
        JFrame frame = new JFrame("Speedometer");
        frame.setSize(500, 150);
        Speedometer instance = new Speedometer();
        instance.init();
        frame.add(instance);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return instance;
    }

    private void init()
    {   
        setLayout(new GridLayout(4, 1));
        _format = new DecimalFormat("000.000");
        add(new JLabel("Shows current speed of car from controller"));
        _wheelSpeedLabel = new JLabel(_format.format(0.0));
        add(_wheelSpeedLabel);
        _segmentLabel = new JLabel("");
        add(_segmentLabel);
        _velocityLabel = new JLabel("");
        add(_velocityLabel);
    }

    public void updateWheelRotationSpeed(float targetWheelRotation)
    {
        _wheelSpeedLabel.setText(_format.format(targetWheelRotation));
    }

    public void updateCurrentSegment(TrajectoryElement currentSegment)
    {
        if(currentSegment != null)
        {
            
            _segmentLabel.setText("Current segment index: + " + currentSegment.getIdx() + System.lineSeparator() +  "Vector: " + formatVector(currentSegment.getVector()) + System.lineSeparator() + "Set Velocity: " + formatVelocity(currentSegment.getVelocity()));
        }
        else
        {
            _segmentLabel.setText("Current segmen is null");
        }
    }

    private String formatVelocity(double velocity)
    {
        return String.format("%.2f", velocity);
    }

    private String formatVector(Vector2D vector)
    {
        return String.format("[(%.2f, %.2f) -> (%.2f, %.2f)], <->: %.2f ", vector.getbX(), vector.getbY(), vector.getdX(), vector.getdY(), vector.getLength());
    }

    public void updateActualVelocity(double[] vehicleVelocity)
    {
        Vector2D planeVelVector = new Vector2D(0.0, 0.0, vehicleVelocity[0], vehicleVelocity[1]);
        double planeVelocity = planeVelVector.getLength();
        _velocityLabel.setText(String.format("v: %.2f m/s, %.2f km/h | x: %.2f, y: %.2f, z: %.2f", planeVelocity, UnitConverter.meterPerSecondToKilometerPerHour(planeVelocity), vehicleVelocity[0], vehicleVelocity[1], vehicleVelocity[2]));
    }
}
