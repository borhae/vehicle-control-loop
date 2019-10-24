package de.joachim.haensel.phd.scenario.experiment.evaluation.database.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.KMeansClustering;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

class KMeansClusteringTests
{
    @Test
    void testLength2Y0Y2()
    {
        KMeansClustering clusterer = new KMeansClustering();
        List<List<Vector2D>> trajectories = new ArrayList<List<Vector2D>>();
        List<Vector2D> t1 = Arrays.asList(new Vector2D[] {
           new Vector2D(0.0, 0.0, 1.0, 0.0),
           new Vector2D(1.0, 0.0, 1.0, 0.0)
        });
        List<Vector2D> t2 = Arrays.asList(new Vector2D[] {
                new Vector2D(0.0, 2.0, 1.0, 0.0),
                new Vector2D(1.0, 2.0, 1.0, 0.0)
         });
        trajectories.add(t1);
        trajectories.add(t2);
        List<Integer> indices = Arrays.asList(new Integer[]{0, 1});
        
        List<Vector2D> expected = Arrays.asList(new Vector2D[] {
                new Vector2D(0.0, 1.0, 1.0, 0.0),
                new Vector2D(1.0, 1.0, 1.0, 0.0)
         });

        List<Vector2D> actual = clusterer.findCenter(indices, trajectories);
        
        assertEquals(expected, actual);
    }

    @Test
    void testLength2Y0Y1()
    {
        KMeansClustering clusterer = new KMeansClustering();
        List<List<Vector2D>> trajectories = new ArrayList<List<Vector2D>>();
        List<Vector2D> t1 = Arrays.asList(new Vector2D[] {
           new Vector2D(0.0, 0.0, 1.0, 0.0),
           new Vector2D(1.0, 0.0, 1.0, 0.0)
        });
        List<Vector2D> t2 = Arrays.asList(new Vector2D[] {
            new Vector2D(0.0, 1.0, 1.0, 0.0),
            new Vector2D(1.0, 1.0, 1.0, 0.0)
        });
        trajectories.add(t1);
        trajectories.add(t2);
        List<Integer> indices = Arrays.asList(new Integer[]{0, 1});
        
        List<Vector2D> expected = Arrays.asList(new Vector2D[] {
                new Vector2D(0.0, 0.5, 1.0, 0.0),
                new Vector2D(1.0, 0.5, 1.0, 0.0)
         });


        List<Vector2D> actual = clusterer.findCenter(indices, trajectories);
        
        assertEquals(expected, actual);
    }
    
    @Test
    void testDoubleApplication()
    {
        KMeansClustering clusterer = new KMeansClustering();
        List<List<Vector2D>> trajectories = new ArrayList<List<Vector2D>>();
        List<Vector2D> t1 = Arrays.asList(new Vector2D[] {
           new Vector2D(0.0, 0.0, 1.0, 0.0),
           new Vector2D(1.0, 0.0, 1.0, 0.0)
        });
        List<Vector2D> t2 = Arrays.asList(new Vector2D[] {
                new Vector2D(0.0, 2.0, 1.0, 0.0),
                new Vector2D(1.0, 2.0, 1.0, 0.0)
         });
        trajectories.add(t1);
        trajectories.add(t2);
        List<Integer> indices = Arrays.asList(new Integer[]{0, 1});
        
        List<Vector2D> expected = Arrays.asList(new Vector2D[] {
                new Vector2D(0.0, 1.0, 1.0, 0.0),
                new Vector2D(1.0, 1.0, 1.0, 0.0)
         });

        List<Vector2D> actual = clusterer.findCenter(indices, trajectories);
        
        assertEquals(expected, actual);

        List<List<Vector2D>> trajectories2 = new ArrayList<List<Vector2D>>();
        List<Vector2D> t12 = Arrays.asList(new Vector2D[] {
           new Vector2D(0.0, 0.0, 1.0, 0.0),
           new Vector2D(1.0, 0.0, 1.0, 0.0)
        });
        List<Vector2D> t22 = Arrays.asList(new Vector2D[] {
            new Vector2D(0.0, 1.0, 1.0, 0.0),
            new Vector2D(1.0, 1.0, 1.0, 0.0)
        });
        trajectories2.add(t12);
        trajectories2.add(t22);
        List<Integer> indices2 = Arrays.asList(new Integer[]{0, 1});
        
        List<Vector2D> expected2 = Arrays.asList(new Vector2D[] {
                new Vector2D(0.0, 0.5, 1.0, 0.0),
                new Vector2D(1.0, 0.5, 1.0, 0.0)
         });


        List<Vector2D> actual2 = clusterer.findCenter(indices2, trajectories2);
        
        assertEquals(expected2, actual2);
        
    }
}
