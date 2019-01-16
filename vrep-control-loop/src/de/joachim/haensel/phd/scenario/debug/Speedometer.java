package de.joachim.haensel.phd.scenario.debug;

import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class Speedometer extends JPanel
{
    private JLabel _wheelSpeedLabel;
    private DecimalFormat _format;
    private JLabel _segmentLabel;
    private JLabel _rawVelocities;
    private JLabel _plannedVelocityLabel;
    private JLabel _computedVelocities;

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
        setLayout(new GridLayout(6, 1));
        _format = new DecimalFormat("000.000");
        add(new JLabel("Shows current speed of car from controller"));
        _wheelSpeedLabel = new JLabel(_format.format(0.0));
        add(_wheelSpeedLabel);
        _segmentLabel = new JLabel("");
        add(_segmentLabel);
        _rawVelocities = new JLabel("");
        add(_rawVelocities);
        _computedVelocities = new JLabel("");
        add(_computedVelocities);
        _plannedVelocityLabel = new JLabel("");
        add(_plannedVelocityLabel);
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

    public void updateVelocities(double[] vehicleVelocity, double plannedVelocity)
    {
        Vector2D planeVelVector = new Vector2D(0.0, 0.0, vehicleVelocity[0], vehicleVelocity[1]);
        double planeVelocity = planeVelVector.getLength();
        double planeVelKMh = UnitConverter.meterPerSecondToKilometerPerHour(planeVelocity);
        double plannedVelKMh = UnitConverter.meterPerSecondToKilometerPerHour(plannedVelocity);
        _rawVelocities.setText(String.format("v: %.2f km/h, %.2f m/s | x: %.2f, y: %.2f, z: %.2f", planeVelKMh, planeVelocity, vehicleVelocity[0], vehicleVelocity[1], vehicleVelocity[2]));
        _plannedVelocityLabel.setText(String.format("planned: %.2f, diff: %.2f", plannedVelKMh, planeVelKMh - plannedVelKMh));
    }

    public void updateVelocities(double[] vehicleVelocity, Vector2D vehicleOrientation, double plannedVelocity)
    {
        Vector2D planeVelVector = new Vector2D(0.0, 0.0, vehicleVelocity[0], vehicleVelocity[1]);
        double planeVelocity = planeVelVector.getLength();
        double planeVelocityKMh = UnitConverter.meterPerSecondToKilometerPerHour(planeVelocity);
        
        double vehicleAngle = Vector2D.computeAngle(new Vector2D(0.0, 0.0, 1.0, 0.0),  vehicleOrientation);
        TMatrix rotationMatrix = TMatrix.rotationMatrix(-vehicleAngle);
        Vector2D vehicleOrientedVelVec = new Vector2D(planeVelVector);
        vehicleOrientedVelVec.transform(rotationMatrix);
        
        double vehicleLongVelKMh = UnitConverter.meterPerSecondToKilometerPerHour(vehicleOrientedVelVec.getdX());
        double vehicleLatVelKMh = UnitConverter.meterPerSecondToKilometerPerHour(vehicleOrientedVelVec.getdY());
        double plannedVelKMh = UnitConverter.meterPerSecondToKilometerPerHour(plannedVelocity);
        _rawVelocities.setText(String.format("v: %.2f | x: %.2f, y: %.2f, z: %.2f (all m/s)", planeVelocity, vehicleVelocity[0], vehicleVelocity[1], vehicleVelocity[2]));
        _computedVelocities.setText(String.format("v_all: %.3f v_long: %.3f, v_lat: %.3f (all km/h)", planeVelocityKMh, vehicleLongVelKMh, vehicleLatVelKMh));
        _plannedVelocityLabel.setText(String.format("planned: %.2f, diff: %.2f", plannedVelKMh, vehicleLongVelKMh - plannedVelKMh));
    }
}
