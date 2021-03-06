package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.math.geometry.Point3D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.ObservationTuple;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.StepDirection;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.TurtleHash;
import de.joachim.haensel.phd.scenario.vehicle.experiment.RecordedTrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.streamextensions.IndexAdder;

public class TestTurtleHashing
{
    public static Stream<Arguments> parameters()
    {
        return Arrays.asList(new Object[][]
        {
//            {"luebeck_extramini_routing_challenge", 15.0, 120.0, 4.0, 4.3, 1.0},
            {"luebeck_40_targets", 15.0, 120.0, 4.0, 4.0, 1.0},
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
        String localTestID = testID + String.format(Locale.US, "%f_%f_%.2f_%.2f_%.2f_", lookahead, maxVelocity, maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration); 
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
            TurtleHash hasher = new TurtleHash(15.1, 5.0, 20);
            TreeMap<Long, List<TrajectoryElement>> configsSorted = new TreeMap<Long, List<TrajectoryElement>>(configurations);
            TreeMap<String, List<Long>> configsHashed = new TreeMap<String, List<Long>>();
            int trjIdx = 0;
            int tooShortSkipped = 0;
            TrajectoryNormalizer.normalizeConfigurationsAndObservations(configurations, observations);
            for (Entry<Long, List<TrajectoryElement>> curPlan : configsSorted.entrySet())
            {
                List<TrajectoryElement> curTrajectory = curPlan.getValue();
                ObservationTuple curObservation = observations.get(curPlan.getKey());
                if(curTrajectory.size() != 20)
                {
                    tooShortSkipped++;
                    continue;
                }
                trjIdx++;
                
                List<int[]> pixels = hasher.pixelate(curTrajectory);
                List<Integer> steps = hasher.createSteps3D(pixels);
//                List<StepDirection> steps = hasher.createSteps(pixels);
                
//                JFrame pixelFrame = new JFrame("pixels");
//                pixelFrame.add(new JLabel(new ImageIcon(createImage(pixels))));
//                pixelFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                pixelFrame.pack();
//                pixelFrame.setVisible(true);
//
//                JFrame arrowFrame = new JFrame("directions");
//                arrowFrame.add(new JLabel(new ImageIcon(createImage(pixels, steps))));
//                arrowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                arrowFrame.pack();
//                arrowFrame.setVisible(true);
                Position2D pZero = new Position2D(0.0, 0.0);
                for(int idx = 0; idx < pixels.size() - 1; idx++)
                {
                    int[] cur = pixels.get(idx);
                    int[] nxt = pixels.get(idx + 1);
//                    System.out.format("p1(%d, %d), p2(%d, %d)\n", cur[0], cur[1], nxt[0], nxt[1]);
                    boolean differentPoints = !TurtleHash.same3D(cur, nxt);
                    boolean connectedPoints = TurtleHash.connected3D(cur, nxt);
                    if(!differentPoints || !connectedPoints)
                    {
                        System.out.println("stop here");
                    }
                    assertThat(String.format("Detected overlap in adjacent pixels p1(%d, %d, %d), p2(%d, %d, %d)", cur[0], cur[1], cur[2], nxt[0], nxt[1], nxt[2]), differentPoints);
                    assertThat(String.format("Adjacent pixels should be connected p1(%d, %d, %d), p2(%d, %d, %d)", cur[0], cur[1], cur[2], nxt[0], nxt[1], nxt[2]), connectedPoints);
                }
                String jsonVoxel = 
                        pixels.stream().map(p -> String.format("[%d, %d, %d, 4278190335]", p[0] - 50, p[1] - 50, p[2])).collect(Collectors.joining(", ", "[", "]"));
                String jsonString = String.format("{\"creator\": \"Zoxel Version 0.6.2\", \"height\": 100, \"width\": 100, \"depth\": 120, \"version\": 1, \"frames\": 1, \"frame1\": %s}", jsonVoxel);
//                writeToFile(jsonString, testID, trjIdx);
                String configurationHash = steps.stream().map(d -> TurtleHash.toBase26(d)).collect(Collectors.joining());
//                String hashVal = steps.stream().map(d -> d.toCode()).collect(Collectors.joining());
                System.out.format("Number of trajectory elements: %d, number of pixels: %d\n", curTrajectory.size(), pixels.size());
                System.out.println(configurationHash);
                //distance_orientation_velocity
                double distance = Position2D.distance(pZero, curObservation.getRearWheelCP());
                double orientation = curObservation.getOrientation().getAngle();
                double velocity = Position2D.distance(pZero, new Position2D(Arrays.copyOfRange(curObservation.getVelocity(), 0, 2)));
                String observationHash = String.format("%02.0f%02.0f%02.0f", distance * 2.5, Math.toDegrees(orientation) / 40.0, velocity / 4.0);
                System.out.println(observationHash);
                String hash = configurationHash + observationHash;
                if(hash.equals(""))
                {
                    System.out.println("why?");
                }
                List<Long> bucket = configsHashed.get(hash);
                if(bucket == null)
                {
                    bucket = new ArrayList<Long>();
                    configsHashed.put(hash, bucket);
                }
                bucket.add(curPlan.getKey());
            }
            ObjectMapper om = new ObjectMapper();
            mapper.writeValue(new File("./res/operationalprofiletest/serializedruns/histogram_trjfirst" + testID + ".json"), configsHashed);
            List<String> histogram = new ArrayList<String>();
            histogram.add("class, amount");
//            histogram.addAll(configsHashed.entrySet().stream().map(entry -> String.format("%-20s %d", entry.getKey(), entry.getValue().size())).collect(Collectors.toList()));
            histogram.addAll(configsHashed.entrySet().stream().map(entry -> String.format("%s, %d", entry.getKey(), entry.getValue().size())).collect(Collectors.toList()));
            Files.write(new File("./res/operationalprofiletest/serializedruns/histogram_trjfirst" + testID + ".txt").toPath(), histogram, Charset.defaultCharset());
            System.out.println(String.format("Number of trajectories: %d, skipped trajectories: %d, number of buckets: %d", configsSorted.size(), tooShortSkipped, configsHashed.size()));
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
    
    private void writeToFile(String jsonString, String testID, int trjIdx)
    {
        List<String> out = new ArrayList<String>();
        out.add(jsonString);
        try
        {
            Files.write(new File("./res/operationalprofiletest/serializedruns/voxel/Zoxel" + testID + trjIdx + ".zox").toPath(), out, Charset.defaultCharset());
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
                cP = new int[][]{cPS[5], cPS[6], cPS[7]};
                break;
            case SE:
                cP = new int[][]{cPS[1], cPS[2], cPS[3]};
                break;
            case SW:
                cP = new int[][]{cPS[7], cPS[0], cPS[1]};
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
            int factor = 4;
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
        int[][] actual = hasher.rasterizeVectorSimple(v);
        int[][] expected = new int[][] {
            {0, 0},
            {1, 1},
            {2, 2},
            {3, 3},
            {4, 4}
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorVerticalLine()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 0.0, 5.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVectorSimple(v);
        int[][] expected = new int[][] {
            {0, 0},
            {0, 1},
            {0, 2},
            {0, 3},
            {0, 4}
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorHorizontalLine()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 5.0, 0.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVectorSimple(v);
        int[][] expected = new int[][] {
            {0, 0},
            {1, 0},
            {2, 0},
            {3, 0},
            {4, 0}
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorRasterizeDegreeBresenham3D()
    {
        Point3D p1 = new Point3D(-1.0, 1.0, 1.0);
        Point3D p2 = new Point3D(5.0, 3.0, -1.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVectorBresenham3D(p1, p2);
        int[][] expected = new int[][] {
            {-1, 1, 1}, 
            {0, 1, 1}, 
            {1, 2, 0},
            {2, 2, 0}, 
            {3, 2, 0}, 
            {4, 3, -1}, 
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorHash45DegreeBresenham()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 5.0, 5.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVectorBresenham(v);
        int[][] expected = new int[][] {
            {0, 0},
            {1, 1},
            {2, 2},
            {3, 3},
            {4, 4},
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorVerticalLineBresenham()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 0.0, 5.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVectorBresenham(v);
        int[][] expected = new int[][] {
            {0, 0},
            {0, 1},
            {0, 2},
            {0, 3},
            {0, 4},
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorHorizontalLineBresenham()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 5.0, 0.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 25);
        int[][] actual = hasher.rasterizeVectorBresenham(v);
        int[][] expected = new int[][] {
            {0, 0},
            {1, 0},
            {2, 0},
            {3, 0},
            {4, 0},
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorHash45DegreeAmanatidesWoo()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 5.0, 5.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 20);
        int[][] actual = hasher.rasterizeVectorAmanatidesWoo(v);
        int[][] expected = new int[][] {
            {0, 0}, 
            {0, 1}, 
            {1, 1}, 
            {1, 2}, 
            {2, 2}, 
            {2, 3}, 
            {3, 3}, 
            {3, 4}, 
            {4, 4}, 
            {4, 5}, 
            {5, 5}
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorVerticalLineAmanatidesWoo()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 0.0, 5.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 20);
        int[][] actual = hasher.rasterizeVectorAmanatidesWoo(v);
        int[][] expected = new int[][] {
            {0, 0},
            {0, 1},
            {0, 2},
            {0, 3},
            {0, 4},
            {0, 5}
        };
        assertArrayEquals(expected, actual, "arrays differ");
    }
    
    @Test
    public void testSingleVectorHorizontalLineAmanatidesWoo()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 5.0, 0.0);
        TurtleHash hasher = new TurtleHash(1, 5.0, 20);
        int[][] actual = hasher.rasterizeVectorAmanatidesWoo(v);
        int[][] expected = new int[][] {
            {0, 0},
            {1, 0},
            {2, 0},
            {3, 0},
            {4, 0},
            {5, 0}
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
