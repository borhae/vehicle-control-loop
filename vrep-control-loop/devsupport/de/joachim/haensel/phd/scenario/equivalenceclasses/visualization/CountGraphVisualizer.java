package de.joachim.haensel.phd.scenario.equivalenceclasses.visualization;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;

import de.joachim.haensel.phd.scenario.equivalenceclasses.visualization.graph.FoldableTree;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ConfigurationObservationTreeCounter;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.CountTreeNode;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class CountGraphVisualizer extends JFrame
{
    private static final long serialVersionUID = -2707712944901661771L;
    private String _experimentName;

    public CountGraphVisualizer(String experimentName)
    {
        super(experimentName);
        _experimentName = experimentName;
    }

    public void init()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Map<Long, ObservationTuple> observations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Ob" + _experimentName),new TypeReference<Map<Long, ObservationTuple>>() {});
            Map<Long, List<TrajectoryElement>> configurations = 
                    mapper.readValue(new File("./res/operationalprofiletest/serializedruns/Co" + _experimentName),new TypeReference<Map<Long, List<TrajectoryElement>>>() {});
            CountTreeNode root = ConfigurationObservationTreeCounter.count(configurations, observations);

            FoldableTree graph = new FoldableTree();
    
            mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, false);
            layout.setUseBoundingBox(false);
            layout.setEdgeRouting(false);
            layout.setLevelDistance(50);
            layout.setNodeDistance(3);
    
            Object parent = graph.getDefaultParent();
            Map<String, Object> edgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
//            edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
            edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
            graph.getModel().beginUpdate();
            try
            {
                Object rootVertice = createVertice(graph, parent, root);
                layout.execute(parent);
            }
            finally
            {
                graph.getModel().endUpdate();
            }
            graph.addListener(mxEvent.FOLD_CELLS, new mxIEventListener() {
    
                @Override
                public void invoke(Object sender, mxEventObject evt)
                {
                    layout.execute(graph.getDefaultParent());
                }
            });
    
            mxGraphComponent graphComponent = new mxGraphComponent(graph);
            MouseWheelListener wheelListener = new MouseWheelListener() {
                
                @Override
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    if(e.getWheelRotation() < 0)
                    {
                        graphComponent.zoomIn();
                    }
                    else
                    {
                        graphComponent.zoomOut();
                    }
                }
            };
            graphComponent.addMouseWheelListener(wheelListener);
            getContentPane().add(graphComponent);
        }
        catch (JsonParseException exc)
        {
            exc.printStackTrace();
        }
        catch (JsonMappingException exc)
        {
            exc.printStackTrace();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private Object createVertice(FoldableTree graph, Object parent, CountTreeNode node)
    {
        List<Object> childVertices = new ArrayList<>();
        for (CountTreeNode curChild : node.getChildren())
        {
            childVertices.add(createVertice(graph, parent, curChild));
        }
        int nrChildren = node.getChildren() != null ? node.getChildren().size() : 0;
        int nrContent = node.getContent() != null ? node.getContent().size() : 0;
//        Object result = graph.insertVertex(parent, null, String.format("ch: %d\nco: %d", nrChildren, nrContent), 0, 0, 60, 30);
        Object result = graph.insertVertex(parent, null, String.format("%d/%d\n%s", nrChildren, nrContent, node.toSmallString()), 0, 0, 60, 60);
        for (Object curChildVertice : childVertices)
        {
            graph.insertEdge(parent, null, "", result, curChildVertice);
        }
        return result;
    }

    public static void main(String[] args)
    {
//        CountGraphVisualizer luebeckFrame = new CountGraphVisualizer("luebeck_10_targets15.000000_120.000000_4.00_4.00_1.00_.json");
        CountGraphVisualizer luebeckFrame = new CountGraphVisualizer("luebeck_20_targets15.000000_120.000000_4.00_4.00_1.00_.json");
        luebeckFrame.init();
        luebeckFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        luebeckFrame.setSize(400, 320);
        luebeckFrame.setVisible(true);

//        CountGraphVisualizer chandigarhFrame = new CountGraphVisualizer("chandigarh_10_targets15.000000_120.000000_4.00_4.00_1.00_.json");
        CountGraphVisualizer chandigarhFrame = new CountGraphVisualizer("chandigarh_20_targets15.000000_120.000000_4.00_4.00_1.00_.json");
        chandigarhFrame.init();
        chandigarhFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chandigarhFrame.setSize(400, 320);
        chandigarhFrame.setVisible(true);
    }
}
