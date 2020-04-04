package de.joachim.haensel.phd.scenario.experiment.runner;

public class ExperimentConfiguration
{
//  should the generation result be shown in the simulator
    private boolean _visualisationInSimulator;
//  should origins/destinations be saved? 
    private boolean _arePositionsToBeSaved;
//  _mapFileName = "luebeck-roads.net.xml";
//  _mapFileName = "chandigarh-roads-lefthand.removed.net.xml";
    private String _mapFileName;
//  should the experiment be run after origin/destination generation
    private boolean _runExperiment;
//  a positive integer number that is not too large (< 10000000000000), seeding the random generator that is the base for road origin and target point generation
    private int _seed;
//  should the map be shown before experiment start (in generation phase)
    private boolean _showMap;
//  should start and endpoints be shown for visual analysis
    private boolean _showStartEndPoints;
//  should the routes be checked for sanity
    private boolean _runRouteSanityCheck;
//  should routes be shown for visual sanity check
    private boolean _showRoutes;
//  fileNameOrOut = city.substring(0, 1).toUpperCase() + city.substring(1) + "points_generatedSeed" + getSeed() + ".txt";
    private String _positionListFileName;
    private boolean _positionListFilenameDefault;
    private boolean _detachSimulator;

    public ExperimentConfiguration()
    {
    }

    public void setDefault()
    {
        _visualisationInSimulator = false;
        _arePositionsToBeSaved = true;
        _mapFileName = "luebeck-roads.net.xml";
        _runExperiment = true;
        _seed = 1001;
        _showMap = false;
        _showStartEndPoints = false;
        _runRouteSanityCheck = true;
        _showRoutes = false;
        String city = compileCityName();
        _positionListFileName = city.substring(0, 1).toUpperCase() + city.substring(1) + "points_generatedSeed" + getSeed() + ".txt";
        _positionListFilenameDefault = true;
        _detachSimulator = false;
    }
    
    public String compileCityName()
    {
        if(_mapFileName.equals("luebeck-roads.net.xml"))
        {
            return "luebeck";
        }
        else if(_mapFileName.equals("chandigarh-roads-lefthand.removed.net.xml"))
        {
            return "chandigarh";
        }
        else
        {
            return "";
        }
    }
    
    public boolean getRunRouteSanityCheck()
    {
        return _runRouteSanityCheck;
    }

    public void setRunRouteSanityCheck(boolean runRouteSanityCheck)
    {
        _runRouteSanityCheck = runRouteSanityCheck;
    }

    public void setVisualisationInSimulator(boolean visualisationInSimulator)
    {
        _visualisationInSimulator = visualisationInSimulator;
    }

    public void setArePositionsToBeSaved(boolean arePositionsToBeSaved)
    {
        _arePositionsToBeSaved = arePositionsToBeSaved;
    }

    public void setMapFileName(String mapFileName)
    {
        _mapFileName = mapFileName;
    }

    public void setRunExperiment(boolean runExperiment)
    {
        _runExperiment = runExperiment;
    }

    public void setSeed(int seed)
    {
        _seed = seed;
    }

    public void setShowMap(boolean showMap)
    {
        _showMap = showMap;
    }

    public void setShowStartEndPoints(boolean showStartEndPoints)
    {
        _showStartEndPoints = showStartEndPoints;
    }

    public void setShowRoutes(boolean showRoutes)
    {
        _showRoutes = showRoutes;
    }

    public void setPositionListFileName(String positionListFileName)
    {
        _positionListFileName = positionListFileName;
    }

    public void setPositionListFilenameDefault(boolean positionListFilenameDefault)
    {
        _positionListFilenameDefault = positionListFilenameDefault;
    }

    public void setDetachSimulator(boolean detachSimulator)
    {
        _detachSimulator = detachSimulator;
    }

    public boolean getVisualisationInSimulator()
    {
        return _visualisationInSimulator;
    }

    public String getMapFileName()
    {
        return _mapFileName;
    }

    public boolean getArePositionsToBeSaved()
    {
        return _arePositionsToBeSaved;
    }

    public boolean getRunExperiment()
    {
        return _runExperiment;
    }

    public int getSeed()
    {
        return _seed;
    }

    public boolean getShowMap()
    {
        return _showMap;
    }

    public boolean getShowStartEndPoints()
    {
        return _showStartEndPoints;
    }

    public boolean getShouldRunRouteSanityCheck()
    {
        return _runRouteSanityCheck;
    }

    public boolean getShowRoutes()
    {
        return _showRoutes;
    }

    public String getPositionListFileName()
    {
        return _positionListFileName;
    }

    public boolean getPositionListFilenameDefault()
    {
        return _positionListFilenameDefault;
    }

    public boolean getDetachSimulator()
    {
        return _detachSimulator;
    }
}
