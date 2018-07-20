package de.joachim.haensel.phd.scenario.equivalenceclasses.builders;

import java.util.ArrayList;

import de.joachim.haensel.phd.scenario.equivalenceclasses.IClassificationResult;

public class ArcsLinesContainer implements IClassificationResult
{
    private ArrayList<IArcsLineContainerElement> _container;

    public ArcsLinesContainer()
    {
        _container = new ArrayList<IArcsLineContainerElement>();
    }
    
    @Override
    public void add(IArcsLineContainerElement element)
    {
        _container.add(element);
    }
}
