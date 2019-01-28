package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;

import org.junit.Test;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class TestCountTreeNode
{
    @Test
    public void testCountTreeNode()
    {
        
        ArrayList<TrajectoryElement> trajectory = new ArrayList<TrajectoryElement>();
        trajectory.add(new TrajectoryElement(new Vector2D(0.0, 0.0, 1.0, 1.0)));
        StateAt state1 = new StateAt(0l, trajectory, new ObservationTuple(new Position2D(0.0, 0.0), new Position2D(0.0, 0.0), new double[]{0.0, 0.0}, 0));
        StateAt state2= new StateAt(0l, trajectory, new ObservationTuple(new Position2D(0.0, 0.0), new Position2D(0.0, 0.0), new double[]{0.0, 0.0}, 0));
        CountTreeNode root = new CountTreeNode(null);
        root.enter(state1);
        root.enter(state2);
        System.out.println("bla");
    }
}
