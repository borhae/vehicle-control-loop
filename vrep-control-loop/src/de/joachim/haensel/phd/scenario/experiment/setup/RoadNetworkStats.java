package de.joachim.haensel.phd.scenario.experiment.setup;

import de.joachim.haensel.phd.scenario.map.RoadMap;

public class RoadNetworkStats
{
    public static void main(String[] args)
    {
//        String networkName = "neumarkRealWorldNoTrains";
        String networkName = args[0];
        RoadMap map = new RoadMap("./res/roadnetworks/" + networkName + ".net.xml");
        int nrOfNodes = map.getNodes().size();
        int nrOfEdges = map.getEdges().size();
        System.out.format("Network: %s has\nNodes: %d\nEdge: s%d\nO(n^3) Algorithm would result in\ncomputations %d", networkName, nrOfNodes, nrOfEdges, cubic(nrOfNodes));
    }

    private static long cubic(int intV)
    {
        long v = (long)intV;
        return v * v * v;
    }
}
