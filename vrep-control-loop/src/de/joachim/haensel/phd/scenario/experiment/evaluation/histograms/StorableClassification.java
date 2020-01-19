package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

import java.util.List;

public class StorableClassification<T>
{
    private int _clusterNr;
    private String _clusteringMethod;
    private List<List<List<Double>>> _members;
    protected T _classifier;
    
    public int getClusterNr()
    {
        return _clusterNr;
    }
    
    public void setClusterNr(int clusterNr)
    {
        _clusterNr = clusterNr;
    }
    
    public String getClusteringMethod()
    {
        return _clusteringMethod;
    }
    
    public void setClusteringMethod(String clusteringMethod)
    {
        _clusteringMethod = clusteringMethod;
    }
    
    public List<List<List<Double>>> getMembers()
    {
        return _members;
    }
    
    public void setMembers(List<List<List<Double>>> members)
    {
        _members = members;
    }
    
    public T getClassifier()
    {
        return _classifier;
    }
    
    public void setClassifier(T classifier)
    {
        _classifier = classifier;
    }
}
