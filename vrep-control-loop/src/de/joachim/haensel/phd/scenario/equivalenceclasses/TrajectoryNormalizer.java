package de.joachim.haensel.phd.scenario.equivalenceclasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TrajectoryNormalizer
{

    public static Map<Long, List<TrajectoryElement>> normalize(Map<Long, List<TrajectoryElement>> segmentBuffers)
    {
        Map<Long, List<TrajectoryElement>> normalized = new HashMap<>();
        for (Entry<Long, List<TrajectoryElement>> entry : segmentBuffers.entrySet())
        {
            normalized.put(entry.getKey(), normalize(entry.getValue()));
        }
        return normalized;
    }

    private static List<TrajectoryElement> normalize(List<TrajectoryElement> trajectory)
    {
        if(trajectory != null && trajectory.size() > 1)
        {
            Vector2D root = trajectory.get(0).getVector();
            Position2D rootBase = root.getBase();
            Position2D rootDir = root.getDir();
            int dirCnt = 1;
            while((rootDir.getX() == 0.0 && rootDir.getY() == 0.0) && dirCnt < trajectory.size())
            {
                rootDir = trajectory.get(dirCnt).getVector().getDir();
                dirCnt++;
            }
            if(rootDir.getX() == 0.0 && rootDir.getY() == 0.0)
            {
                return trajectory;
            }
            double angle = Math.atan2(rootDir.getY(), rootDir.getX());
            trajectory.stream().forEach(element -> element.getVector().sub(rootBase.getX(), rootBase.getY()).transform(TMatrix.rotationMatrix(-angle)));

            return trajectory;
        }
        else if(trajectory.size() == 1)
        {
            Vector2D root = trajectory.get(0).getVector();
            root.resetBase(0.0, 0.0);
            Position2D rootDir = root.getDir();
            if(!(rootDir.getX() == 0.0 && rootDir.getY() == 0.0))
            {
                double angle = Math.atan2(rootDir.getY(), rootDir.getX());
                root.transform(TMatrix.rotationMatrix(-angle));
            }
        }
        return trajectory;
    }

    public static void normalizeConfigurationsAndObservations(Map<Long, List<TrajectoryElement>> configurationBuffers, Map<Long, ObservationTuple> observations)
    {
        for (Entry<Long, List<TrajectoryElement>> entry : configurationBuffers.entrySet())
        {
            normalize(entry.getValue(), observations.get(entry.getKey()));
        }
    }

    private static void normalize(List<TrajectoryElement> trajectory, ObservationTuple observationTuple)
    {
        if(trajectory != null && trajectory.size() > 1)
        {
            Vector2D rootTrajectoryElement = trajectory.get(0).getVector();
            Position2D rootBase = rootTrajectoryElement.getBase();
            Position2D rootDir = rootTrajectoryElement.getDir();
            int dirCnt = 1;
            while((rootDir.getX() == 0.0 && rootDir.getY() == 0.0) && dirCnt < trajectory.size())
            {
                rootDir = trajectory.get(dirCnt).getVector().getDir();
                dirCnt++;
            }
            if(!(rootDir.getX() == 0.0 && rootDir.getY() == 0.0))
            {
                double angle = Math.atan2(rootDir.getY(), rootDir.getX());
                TMatrix matrix = TMatrix.rotationMatrix(-angle);
                trajectory.stream().forEach(element -> element.getVector().sub(rootBase.getX(), rootBase.getY()).transform(matrix));

                observationTuple.transform(rootBase, matrix);
            }
        }
        else if(trajectory.size() == 1)
        {
            Vector2D root = trajectory.get(0).getVector();
            root.resetBase(0.0, 0.0);
            Position2D rootDir = root.getDir();
            if(!(rootDir.getX() == 0.0 && rootDir.getY() == 0.0))
            {
                double angle = Math.atan2(rootDir.getY(), rootDir.getX());
                root.transform(TMatrix.rotationMatrix(-angle));
            }
        }
    }
}
