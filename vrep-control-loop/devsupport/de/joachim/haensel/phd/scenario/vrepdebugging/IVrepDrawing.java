package de.joachim.haensel.phd.scenario.vrepdebugging;

import java.awt.Color;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public interface IVrepDrawing
{
    public void registerDrawingObject(String key, DrawingType type, Color red);

    /**
     * Update the drawing object formerly created by the {@code drawVector(...)} method
     * @param color to which the drawing object should change
     * @param currentSegmentDebugKey of the drawing object that should be updated 
     * @param vector the new location
     */
    public void updateLine(String objectKey, Vector2D vector, double zValue, Color color);

    public void updateCircle(String objectKey, Position2D position, double zValue, double radius, Color color);

    public void removeAllDrawigObjects();
}
