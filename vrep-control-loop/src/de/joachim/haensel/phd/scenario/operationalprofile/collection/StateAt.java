package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class StateAt
{
    private Long _curTimeStamp;
    private List<TrajectoryElement> _trajectory;
    private ObservationTuple _observationTuple;
    private VelocityNode _root;
    

    public StateAt(Long curTimeStamp, List<TrajectoryElement> trajectory, ObservationTuple observationTuple)
    {
        _curTimeStamp = curTimeStamp;
        _trajectory = trajectory;
        _observationTuple = observationTuple;
        _root = new VelocityNode(observationTuple);
        AngleNode angleTreeNode = new AngleNode(observationTuple, trajectory.get(0));
        _root.setNext(angleTreeNode);
        DisplacementNode displacementElem = new DisplacementNode(observationTuple, trajectory.get(0));
        angleTreeNode.setNext(displacementElem);
        TrajectoryElement previousElem = trajectory.get(0);

        SetVelocityNode setVelocityNode = new SetVelocityNode(previousElem.getVelocity());
        displacementElem.setNext(setVelocityNode);
        
        for (TrajectoryElement curElem : trajectory)
        {

            SetAngleNode setAngleNode = new SetAngleNode(previousElem.getVector(), curElem.getVector());
            setVelocityNode.setNext(setAngleNode);
            
            setVelocityNode = new SetVelocityNode(curElem.getVelocity());
            setAngleNode.setNext(setVelocityNode);
            
            previousElem = curElem;
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
