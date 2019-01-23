package de.joachim.haensel.phd.scenario.map;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public interface IStreetSection
{
    public double getDistance(Position2D position);

    public Vector2D getAPosition();
}
