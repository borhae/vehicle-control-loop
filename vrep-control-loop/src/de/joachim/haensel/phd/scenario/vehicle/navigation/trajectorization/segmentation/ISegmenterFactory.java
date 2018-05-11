package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

public interface ISegmenterFactory
{
    public ISegmenter create(double segmentSize);
}
