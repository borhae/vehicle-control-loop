package de.joachim.haensel.vrepshapecreation.dummy;

import java.util.List;

import de.joachim.haensel.vrepshapecreation.parameters.Parameters;

public class DummyParameters extends Parameters
{
    private Float _size;

    public void setSize(Float size)
    {
        _size = size;
    }

    @Override
    protected void populateInts(List<Integer> paramList)
    {
        //nothing to add here since a dummy doesn't have any integers to set
    }

    @Override
    protected void populateFloats(List<Float> paramList)
    {
        paramList.add(_size);
    }
}
