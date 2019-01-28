package de.joachim.haensel.phd.scenario.equivalenceclasses.visualization.graph;

import java.util.ArrayList;
import java.util.List;

import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.view.mxGraph;

/**
 * A foldable directed acyclic graph (DAG) where each child has only one parent. AKA a Tree.
 * 
 * @author some programmer
 *
 */
public class FoldableTree extends mxGraph
{
    /**
     * Need to add some conditions that will get us the expand/collapse icon on the vertex.
     */
    @Override
    public boolean isCellFoldable(Object cell, boolean collapse)
    {
        //I want to keep the original behavior for groups in case I use a group someday.
        boolean result = super.isCellFoldable(cell, collapse);
        if(!result)
        {
            //I also want cells with outgoing edges to be foldable...
            return this.getOutgoingEdges(cell).length > 0;
        }
        return result;
    }

    /**
     * Need to define how to fold cells for our DAG. In this case we want to traverse the tree collecting
     * all child vertices and then hide/show them and their edges as needed. 
     */
    @Override
    public Object[] foldCells(boolean collapse, boolean recurse, Object[] cells, boolean checkFoldable)
    {
        //super.foldCells does this so I will too...
        if(cells == null)
        {
            cells = getFoldableCells(getSelectionCells(), collapse);
        }

        this.getModel().beginUpdate();

        try
        {           
            toggleSubtree(this, cells[0], !collapse);
            this.model.setCollapsed(cells[0], collapse);
            fireEvent(new mxEventObject(mxEvent.FOLD_CELLS, "cells", cells, "collapse", collapse, "recurse", recurse));
        }
        finally
        {
            this.getModel().endUpdate();
        }

        return cells;
    }

    // Updates the visible state of a given subtree taking into
    // account the collapsed state of the traversed branches
    private void toggleSubtree(mxGraph graph, Object cellSelected, boolean show)
    {
        List<Object> cellsAffected = new ArrayList<>();
        graph.traverse(cellSelected, true, new mxICellVisitor() {                   
                    @Override
                    public boolean visit(Object vertex, Object edge) {
                        // We do not want to hide/show the vertex that was clicked by the user to do not
                        // add it to the list of cells affected.
                        if(vertex != cellSelected)
                        {
                            cellsAffected.add(vertex);
                        }

                        // Do not stop recursing when vertex is the cell the user clicked. Need to keep
                        // going because this may be an expand.
                        // Do stop recursing when the vertex is already collapsed.
                        return vertex == cellSelected || !graph.isCellCollapsed(vertex); 
                    }
                });

        graph.toggleCells(show, cellsAffected.toArray(), true/*includeEdges*/);     
    }
}