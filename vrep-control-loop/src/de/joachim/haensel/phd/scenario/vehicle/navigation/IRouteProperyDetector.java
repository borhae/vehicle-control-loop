package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public interface IRouteProperyDetector
{
    public boolean holds(List<Line2D> result, Line2D curLine, Line2D nextLine, Vector2D curLineV, Vector2D nextLineV);
}
