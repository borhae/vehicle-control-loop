package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.CountTreeNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.EquivalenClassEntry;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TestCountTreeNode
{
    @Test
    public void testCountTreeNode()
    {
        
        ArrayList<TrajectoryElement> trajectory = new ArrayList<TrajectoryElement>();
        trajectory.add(new TrajectoryElement(new Vector2D(0.0, 0.0, 1.0, 1.0)));
        EquivalenClassEntry state1 = new EquivalenClassEntry(0l, trajectory, new ObservationTuple(new Position2D(0.0, 0.0), new Position2D(0.0, 0.0), new double[]{0.0, 0.0}, 0));
        EquivalenClassEntry state2= new EquivalenClassEntry(0l, trajectory, new ObservationTuple(new Position2D(0.0, 0.0), new Position2D(0.0, 0.0), new double[]{0.0, 0.0}, 0));
        CountTreeNode root = new CountTreeNode(null);
        root.enter(state1);
        root.enter(state2);
        System.out.println("bla");
    }
}
