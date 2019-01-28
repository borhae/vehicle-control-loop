package de.joachim.haensel.phd.scenario.operationalprofile.collection;

public interface ICountListElem
{
    public void setNext(ICountListElem elem);
    public ICountListElem next();
    public int getHashRangeIdx();
    public double getNumericalValue();
    public double getNormyValue(); // km/h instead of m/s and degrees instead of radians :)
    public void accept(OCStats stats);
}
