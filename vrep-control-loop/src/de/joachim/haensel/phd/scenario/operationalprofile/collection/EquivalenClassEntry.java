package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.List;

import de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes.AngleNode;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes.DisplacementNode;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes.LeafNode;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes.SetAngleNode;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes.SetVelocityNode;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes.VelocityNode;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

/**
 * Produces linked list with the following elements:
 * VelocityNode, AngleNode, DisplacementNode, [SetVelocity, SetAngle, ..., SetVelocity], LeafNode 
 * @author dummy
 *
 */
public class EquivalenClassEntry 
{
    private Long _curTimeStamp;
    private List<TrajectoryElement> _trajectory;
    private ObservationTuple _observationTuple;
    private VelocityNode _root;
    

    public EquivalenClassEntry(Long curTimeStamp, List<TrajectoryElement> trajectory, ObservationTuple observationTuple)
    {
        _curTimeStamp = curTimeStamp;
        _trajectory = trajectory;
        _observationTuple = observationTuple;
        _root = new VelocityNode(observationTuple);
        AngleNode angleTreeNode = new AngleNode(observationTuple, trajectory.get(0));
        _root.setNext(angleTreeNode);
        DisplacementNode displacementElem = new DisplacementNode(observationTuple, trajectory.get(0));
        angleTreeNode.setNext(displacementElem);

        SetVelocityNode setVelocityNode = new SetVelocityNode(trajectory.get(0).getVelocity());
        displacementElem.setNext(setVelocityNode);
        
        for(int idx = 1; idx < trajectory.size(); idx++)
        {
            TrajectoryElement curElem = trajectory.get(idx);
            TrajectoryElement previousElem = trajectory.get(idx -1);
            
            SetAngleNode setAngleNode = new SetAngleNode(previousElem.getVector(), curElem.getVector());
            setVelocityNode.setNext(setAngleNode);
            
            setVelocityNode = new SetVelocityNode(curElem.getVelocity());
            setAngleNode.setNext(setVelocityNode);
        }
        LeafNode leafNode = new LeafNode(trajectory, observationTuple);
        setVelocityNode.setNext(leafNode);
    }


    public VelocityNode getRoot()
    {
        return _root;
    }


    public List<TrajectoryElement> getConfiguration()
    {
        return _trajectory;
    }
}
