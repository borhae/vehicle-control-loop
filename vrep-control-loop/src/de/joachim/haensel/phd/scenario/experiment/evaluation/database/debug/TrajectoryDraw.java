package de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;

public class TrajectoryDraw
{
    private Chart _chart;
    private int _range;
    private HashMap<Integer, List<LineStrip>> _drawnLines;
    private int _cnt;
    private int _maxTrajectoriesToKeep;

    public TrajectoryDraw(Chart chart)
    {
        _chart = chart;
        _drawnLines = new HashMap<Integer, List<LineStrip>>();
        _cnt = 0;
    }

    public void drawTrajectoriesByIndex(List<Integer> trajectoryIndices, List<double[][]> allTrajectories, int stepInRange)
    {
        if(stepInRange > _maxTrajectoriesToKeep)
        {
            List<LineStrip> obsoleteLines = _drawnLines.get(stepInRange - (_maxTrajectoriesToKeep + 1));
            obsoleteLines.stream().forEach(line -> _chart.getScene().getGraph().remove(line));
        }

        List<double[][]> trajectories = trajectoryIndices.stream().map(idx -> allTrajectories.get(idx)).collect(Collectors.toList());

        List<LineStrip> lines = trajectoriesToLines(trajectories);
        _drawnLines.put(_cnt, lines);
        float brightness = (float)stepInRange / (float)_range;
        IntStream.range(0, lines.size()).forEach(idx -> setColor(lines.get(idx), idx, lines.size(), 1.0f, brightness));
        _chart.getScene().getGraph().add(lines);
        _cnt++;
    }
    
    public void drawTrajectories(List<double[][]> trajectories, int stepInRange)
    {
        if(stepInRange > _maxTrajectoriesToKeep)
        {
            List<LineStrip> obsoleteLines = _drawnLines.get(stepInRange - (_maxTrajectoriesToKeep + 1));
            obsoleteLines.stream().forEach(line -> _chart.getScene().getGraph().remove(line));
        }

        List<LineStrip> lines = trajectoriesToLines(trajectories);
        _drawnLines.put(_cnt, lines);
        float brightness = (float)stepInRange / (float)_range;
        IntStream.range(0, lines.size()).forEach(idx -> setColor(lines.get(idx), idx, lines.size(), 1.0f, brightness));
        _chart.getScene().getGraph().add(lines);
        _cnt++;
    }

    public void drawCluster(List<double[][]> trajectoryArrrays, Map<Integer, List<Integer>> result)
    {
        List<List<Coord3d>> allPaths = trajectoryArrrays.parallelStream().map(t -> arrayToChartPath(t)).collect(Collectors.toList());
        Map<Integer, List<LineStrip>> lineCluster = 
                result.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().parallelStream().map(idx -> new LineStrip(allPaths.get(idx))).collect(Collectors.toList())));
        
        int numOfClusters = lineCluster.keySet().size();
        System.out.println("assigning colors");
        lineCluster.entrySet().stream().forEach(entry -> entry.getValue().stream().forEach(line -> setColor(line, entry.getKey(), numOfClusters, 5)));

        System.out.println("assigning width, display true, showpoints true and wireframe true");
        lineCluster.entrySet().parallelStream().forEach(entry -> entry.getValue().parallelStream().forEach(line -> configureLine(line)));
        
        List<LineStrip> lines = lineCluster.values().stream().flatMap(cluster -> cluster.stream()).collect(Collectors.toList());
        System.out.println("setting up chart");
        System.out.println("adding lines to chart");
        _chart.getScene().getGraph().add(lines);
    }

    private List<Coord3d> arrayToChartPath(double[][] trajectory)
    {
        return Arrays.asList(trajectory).stream().map(tE -> new Coord3d(tE[0], tE[1], tE[2])).collect(Collectors.toList());
    }

    private List<LineStrip> trajectoriesToLines(List<double[][]> centers0)
    {
        List<List<Coord3d>> chartPaths = centers0.parallelStream().map(trajectory -> arrayToChartPath(trajectory)).collect(Collectors.toList());
        return chartPaths.parallelStream().map(chartPath -> new LineStrip(chartPath)).map(line -> configureLine(line)).collect(Collectors.toList());
    }

    private LineStrip configureLine(LineStrip line)
    {
        line.setWireframeWidth(1.0f); 
        line.setFaceDisplayed(true); 
        line.setShowPoints(true); 
        line.setWireframeDisplayed(true);
        return line;
    }
    
    private void setColor(LineStrip line, int idx, int range, int alpha)
    {
        float hue = ((float)idx)/((float)range);
        int rgbCompund = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        int red = (rgbCompund >> 16) & 0xFF;
        int green = (rgbCompund >> 8) & 0xFF; 
        int blue = (rgbCompund >> 0) & 0xFF;
        line.setWireframeColor(new Color(red, green, blue, alpha));
    }
    
    private void setColor(LineStrip line, int idx, int range, float saturation, float brightness)
    {
        float hue = ((float)idx)/((float)range);
        int rgbCompund = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        int red = (rgbCompund >> 16) & 0xFF;
        int green = (rgbCompund >> 8) & 0xFF; 
        int blue = (rgbCompund >> 0) & 0xFF;
        line.setWireframeColor(new Color(red, green, blue, 128));
    }

    public void setRange(int range)
    {
        _range = range;
    }

    public void setMaxTrajectoriesToKeep(int maxTrajectoriesToKeep)
    {
        _maxTrajectoriesToKeep = maxTrajectoriesToKeep;
    }
}
