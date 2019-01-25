package de.joachim.haensel.phd.scenario.operationalprofile.collection;

public interface ICountListElem
{
    public void setNext(ICountListElem elem);
    public ICountListElem next();
    public int getHashRangeIdx();
    public double getNumericalValue();
}
