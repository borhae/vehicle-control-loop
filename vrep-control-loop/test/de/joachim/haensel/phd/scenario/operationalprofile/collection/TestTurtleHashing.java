package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.experiment.RecordedTrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;

public class TestTurtleHashing
{
    public static Stream<Arguments> parameters()
    {
        return Arrays.asList(new Object[][]
        {
            {"luebeck_extramini_routing_challenge", 15.0, 120.0, 4.0, 4.3, 1.0},
//            {"luebeck_small", 15.0, 120.0, 4.0, 4.3, 1.0},
//          {"luebeck_mini_routing_challenge", 15.0, 120.0, 4.0, 4.3, 1.0},
//          {"luebeck_10_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
//          {"chandigarh_10_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
//            {"chandigarh_20_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
//            {"luebeck_20_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
        }).stream().map(params -> Arguments.of(params));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testReadSimulationRun(String testID, double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration)
    {
        String localTestID = testID + String.format("%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration); 
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Map<Long, ObservationTuple> observations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Ob" + localTestID + ".json"),new TypeReference<Map<Long, ObservationTuple>>() {});
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Co" + localTestID + ".json"),new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            List<RecordedTrajectoryElement> trajectoryRecordings = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/TrRe" + localTestID + ".json"), new TypeReference<List<RecordedTrajectoryElement>>() {});
            List<Position2D> plannedPath = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Plan" + localTestID + ".json"), new TypeReference<List<Position2D>>() {});
            timeDistanceSimpleStats(trajectoryRecordings, plannedPath);
            TurtleHash hasher = new TurtleHash(1, 5.0, 20);
            TreeMap<Long, List<TrajectoryElement>> configsSorted = new TreeMap<Long, List<TrajectoryElement>>(configurations);
            for (Entry<Long, List<TrajectoryElement>> curPlan : configsSorted.entrySet())
            {
                List<int[]> pixels = hasher.pixelate(curPlan.getValue());
                List<StepDirection> steps = hasher.createSteps(pixels);
                
                JFrame pixelFrame = new JFrame("pixel");
                pixelFrame.add(new JLabel(new ImageIcon(createImage(pixels))));
                pixelFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                pixelFrame.pack();
                pixelFrame.setVisible(true);

                JFrame arrowFrame = new JFrame("directions");
                arrowFrame.add(new JLabel(new ImageIcon(createImage(pixels, steps))));
                arrowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                arrowFrame.pack();
                arrowFrame.setVisible(true);
                System.out.println("stop");
            }
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
    
    private Image createImage(List<int[]> pixels)
    {
        BufferedImage image = new BufferedImage(1200, 1200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        pixels.stream().map(pixel -> new int[]{pixel[0] * 8, pixel[1] * 8}).map(IndexAdder.indexed()).forEachOrdered(elem -> draw(g, elem));
        return image;
    }

    private Image createImage(List<int[]> hash, List<StepDirection> steps)
    {
        BufferedImage image = new BufferedImage(1200, 1200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        hash.stream().map(pixel -> new int[]{pixel[0] * 8, pixel[1] * 8}).map(IndexAdder.indexed()).forEachOrdered(elem -> draw(g, elem, steps));
        return image;
    }

    private void draw(Graphics2D g, IndexAdder<int[]> elem, List<StepDirection> steps)
    {
        int[] location = elem.v();
        int idx = elem.idx();
        StepDirection direction; 
        if(idx < steps.size())
        {
            direction = steps.get(idx);
        }
        else
        {
            direction = StepDirection.C;
        }
        drawArrow(g, location, direction);
    }

    private void drawArrow(Graphics2D g2, int[] p, StepDirection stepDir)
    {
        int[][] cPS = new int[][] {
            {0, 0},//0
            {1, 0},//1
            {2, 0},//2
            {2, 1},//3
            {2, 2},//4
            {1, 2},//5
            {0, 2},//6
            {0, 1},//7
        };
        int[][] cP = null;
        switch (stepDir)
        {
            case N:
                cP = new int[][]{cPS[7], cPS[5], cPS[3]};
                break;
            case S:
                cP = new int[][]{cPS[7], cPS[1], cPS[3]};
                break;
            case E:
                cP = new int[][]{cPS[1], cPS[3], cPS[5]};
                break;
            case W:
                cP = new int[][]{cPS[1], cPS[7], cPS[5]};
                break;
            case NE:
                cP = new int[][]{cPS[5], cPS[4], cPS[3]};
                break;
            case NW:
                cP = new int[][]{cPS[7], cPS[6], cPS[5]};
                break;
            case SE:
                cP = new int[][]{cPS[1], cPS[2], cPS[3]};
                break;
            case SW:
                cP = new int[][]{cPS[7], cPS[3], cPS[0]};
                break;
            default:
                cP = new int[][]{cPS[7], cPS[1], cPS[3]};
                break;
        }
        if(stepDir == StepDirection.C)
        {
            g2.drawRect(p[0] - 4, p[1] - 4, 8, 8);
        }
        else
        {
            int factor = 2;
            int x0 = cP[0][0] * factor + p[0];
            int y0 = cP[0][1] * factor + p[1];
            int x1 = cP[1][0] * factor + p[0];
            int y1 = cP[1][1] * factor + p[1];
            int x2 = cP[2][0] * factor + p[0];
            int y2 = cP[2][1] * factor + p[1];
            g2.drawLine(x0, y0, x1, y1);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void garbage(Graphics2D g2, int[] p, StepDirection stepDir)
    {
        int xD = 0;
        int yD = 0;
        switch (stepDir)
        {
            case N:
                xD = 1;
                break;
            case S:
                xD = -1;
                break;
            case E:
                yD = 1;
                break;
            case W:
                yD = -1;
                break;
            case NE:
                xD = 1;
                yD = 1;
                break;
            case NW:
                xD = 1;
                yD = -1;
                break;
            case SE:
                xD = -1;
                yD = 1;
                break;
            case SW:
                xD = -1;
                yD = -1;
                break;
            default:
                break;
        }
        if(stepDir == StepDirection.C)
        {
            g2.drawRect(p[0] - 4, p[1] - 4, 8, 8);
        }
        else
        {
            int size = 8; 
            int xB = p[0] - xD * size;
            int yB = p[1] - yD * size;
            int xT = p[0] + xD * size;
            int yT = p[1] + yD * size;
            
            Position2D norm = new Position2D(xT - xB, yT - yB);
            norm.normalize();
            
            double tipSize = 4.0;
            
            Position2D leftWing = norm.copy().transform(new TMatrix(tipSize, 0.0, 0.0, 1.25 * Math.PI));
            Position2D rightWing = norm.copy().transform(new TMatrix(tipSize, 0.0, 0.0, 0.75 * Math.PI));
            g2.drawLine((int)xT, (int)yT, (int)(xT + leftWing.getX()), (int)(yT + leftWing.getY()));
            g2.drawLine((int)xT, (int)yT, (int)(xT + rightWing.getX()), (int)(yT + rightWing.getY()));
        }
    }
    
    private void draw(Graphics2D g, IndexAdder<int[]> elem)
    {
        int[] pixel = elem.v();
        g.drawRect(pixel[0] - 4, pixel[1] - 4, 8, 8);
    }
    
    @Test
    public void testSingleVectorHash45Degree()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 5.0, 5.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVector(v);
        int[][] expected = new int[][] {
            {125, 125},
            {126, 126},
            {127, 127},
            {128, 128},
            {129, 129},
            {130, 130},
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorHorizontalLine()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 5.0, 0.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVector(v);
        int[][] expected = new int[][] {
            {125, 125},
            {126, 125},
            {127, 125},
            {128, 125},
            {129, 125},
            {130, 125},
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }

    @Test
    public void testSingleVectorVerticalLine()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 0.0, 5.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVector(v);
        int[][] expected = new int[][] {
            {125, 125},
            {125, 126},
            {125, 127},
            {125, 128},
            {125, 129},
            {125, 130},
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }

    private void timeDistanceSimpleStats(List<RecordedTrajectoryElement> trajectoryRecordings, List<Position2D> plannedPath)
    {
        
        RecordedTrajectoryElement firstRecord = trajectoryRecordings.get(0);
        RecordedTrajectoryElement lastRecord = trajectoryRecordings.get(trajectoryRecordings.size() - 1);
        long sysTimeSpanMillis = lastRecord.getSysTime() - firstRecord.getSysTime();
        long simTimeSpanMillis = lastRecord.getSimTime() - firstRecord.getSimTime();
        
        RecordedTrajectoryElement lastElem = firstRecord;
        double actualDistance = 0.0;
        for(int idx = 1; idx < trajectoryRecordings.size(); idx++)
        {
            RecordedTrajectoryElement curElem = trajectoryRecordings.get(idx);
            actualDistance += curElem.getPos().distance(lastElem.getPos());
            lastElem = curElem;
        }

        double plannedDistance = 0.0;
        Position2D firstPlanPos = plannedPath.get(0);
        Position2D lastPos = firstPlanPos;
        for (int idx = 0; idx < plannedPath.size(); idx++)
        {
            Position2D curPos = plannedPath.get(idx);
            plannedDistance += curPos.distance(lastPos);
            lastPos = curPos;
        }
        Duration sysTimeSpan = Duration.ofMillis(sysTimeSpanMillis);
        Duration simTimeSpan = Duration.ofMillis(simTimeSpanMillis);
        System.out.println(String.format("systime: %s, simTime %s, distance actual: %f, distance planned: %f", humanReadableFormat(sysTimeSpan), humanReadableFormat(simTimeSpan), actualDistance, plannedDistance));
    }

    public static String humanReadableFormat(Duration duration) 
    {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }
}
