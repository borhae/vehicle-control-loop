package de.joachim.haensel.phd.scenario.layerinterface;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import sumobindings.EdgeType;
import sumobindings.LaneType;

public class RandomMapPositionCreator
{
    /**
     * Chooses a random street from the map, chooses a random lane from the
     * random street and on this lane a random point is chosen.
     * @param roadMap the map on which a random point should be picked
     * @return a random position on the map
     */
    public static Position2D createRandomPositonOnStree(RoadMap roadMap)
    {
        List<EdgeType> edges = roadMap.getEdges();
        MersenneTwister randomGen = new MersenneTwister();
        EdgeType randomEdge = edges.get(randomGen.nextInt(edges.size()));
        List<LaneType> lanes = randomEdge.getLane();
        LaneType randomLane = lanes.get(randomGen.nextInt(lanes.size()));
        List<Line2D> lines = RoadMap.createLines(randomLane.getShape().split(" "));
        Line2D randomLine = lines.get(randomGen.nextInt(lines.size()));
        Vector2D vector = new Vector2D(randomLine);
        vector.setLength(randomGen.nextDouble() * vector.getLength());
        return vector.getTip();
    }
}
