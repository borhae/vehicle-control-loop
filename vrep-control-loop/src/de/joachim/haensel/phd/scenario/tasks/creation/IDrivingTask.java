package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;

public interface IDrivingTask
{
    public static Vector2D computeOrientation(RoadMap roadMap, Position2D startPosition, Position2D targetPosition)
    {
        Navigator nav = new Navigator(roadMap);
        List<Line2D> route = nav.getRoute(startPosition, targetPosition);
        
        Vector2D firstRoadSection = new Vector2D(route.get(0));
        return firstRoadSection;
    }
}
