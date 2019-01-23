package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.List;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;

public interface IDrivingTask
{
    public static double DEFAULT_PURE_PURSUIT_LOOKAHEAD = 20.0;
    public static double DEFAULT_MAX_VELOCITY = 50; //km/h get's translated later on
    public static final double DEFAULT_MAX_LONGITUDINAL_DECCELERATION = 8.0;
    public static final double DEFAULT_MAX_LONGITUDINAL_ACCELERATION = 2.0;
    public static final double DEFAULT_MAX_LATERAL_ACCELERATION = 1.5;

    
    public static Vector2D computeOrientation(RoadMap roadMap, Position2D startPosition, Position2D targetPosition)
    {
        Navigator nav = new Navigator(roadMap);
        List<Line2D> route = nav.getRoute(startPosition, targetPosition);
        
        Vector2D firstRoadSection = new Vector2D(route.get(0));
        return firstRoadSection;
    }
    
    public void setControlParams(double lookahead, double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration);
}
