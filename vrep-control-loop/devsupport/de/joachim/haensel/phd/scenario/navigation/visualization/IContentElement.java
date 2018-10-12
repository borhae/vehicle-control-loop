package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.Color;
import java.awt.Stroke;

public interface IContentElement
{
    public VisualizerContentType getType();

    public double[][] getContent();

    public Color getColor();

    public Stroke getStroke();
}
