package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

public interface ISegmenterFactory
{
    ISegmenter create(double segmentSize);
}
