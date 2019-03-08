package de.joachim.haensel.phd.scenario.experiment.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.experiment.RecordedTrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public class RegularSavingTrajectoryRecorder implements ILowerLayerControl, INavigationListener
{
    private IActuatingSensing _actuatorsSensors;
    private List<RecordedTrajectoryElement> _trajectoryRecord;
    private List<Position2D> _plannedTrajectory;
    private int _controlCnt;
    private List<RecordedTrajectoryElement> _trajectoryintervalToSave;
    private List<RecordedTrajectoryElement> _trajectoryIntervalToSaveCopy; //needed to avoid concurrent access
    private ArrayList<Position2D> _plannedTrajectoryToSave;
    private ArrayList<Position2D> _plannedTrajectoryToSaveCopy; //needed to avoid concurrent access
    private ObjectMapper _trajectoryRecordMapper;
    private ObjectMapper _planMapper;
    private ObjectMapper _finalMapper;
    private int _planSaveInterval;
    private int _controlSaveInterval;
    private int _planCnt;
    private String _testID;
    private Runnable _saveTrajectoryRunnable;
    private Runnable _savePlanRunnable;
    private ExecutorService _threadPool;
    private String _baseOutputDirectory;

    public RegularSavingTrajectoryRecorder(int planSaveInterval, int controlSaveInterval, String baseOutputDirectory, String testID)
    {
        _baseOutputDirectory = baseOutputDirectory;
        _planSaveInterval = planSaveInterval;
        _controlSaveInterval = controlSaveInterval;
        _trajectoryRecord = new ArrayList<>();
        _plannedTrajectory = new ArrayList<>();
        _controlCnt = 0;
        _planCnt = 0;
        _trajectoryintervalToSave = new ArrayList<>();
        _plannedTrajectoryToSave = new ArrayList<>();
        _finalMapper = new ObjectMapper();
        _trajectoryRecordMapper = new ObjectMapper();
        _planMapper = new ObjectMapper();
        _testID = testID;
        _threadPool = Executors.newFixedThreadPool(6);
        _saveTrajectoryRunnable = new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    _trajectoryRecordMapper.writeValue(new File(baseOutputDirectory + "TrRe" + _testID + "part" + _controlCnt + ".json"), _trajectoryIntervalToSaveCopy);
                }
                catch (IOException exc)
                {
                    exc.printStackTrace();
                }
            }
        };
        _savePlanRunnable = new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    _planMapper.writeValue(new File(baseOutputDirectory + "Plan" + _testID + "part" + _planCnt + ".json"), _plannedTrajectoryToSaveCopy);
                }
                catch (IOException exc)
                {
                    exc.printStackTrace();
                }
            }
        };
    }
    
    @Override
    public void controlEvent()
    {
        RecordedTrajectoryElement trajectoryElement = 
                new RecordedTrajectoryElement(new Position2D(_actuatorsSensors.getPosition()), _actuatorsSensors.getTimeStamp(), System.currentTimeMillis());
        _trajectoryRecord.add(trajectoryElement);
        _trajectoryintervalToSave.add(trajectoryElement);
        if((_controlCnt % _controlSaveInterval == 0) && (_trajectoryintervalToSave != null) && !_trajectoryintervalToSave.isEmpty())
        {
            _trajectoryIntervalToSaveCopy = new ArrayList<RecordedTrajectoryElement>(_trajectoryintervalToSave); // TODO too bad if this is set while the saving thread tries to copy it... :(
            _threadPool.execute(_saveTrajectoryRunnable);
            _trajectoryintervalToSave = new ArrayList<RecordedTrajectoryElement>();
        }
        _controlCnt++;
    }

    @Override
    public void driveTo(Position2D position)
    {
    }

    @Override
    public void clearSegmentBuffer()
    {
    }

    @Override
    public void clearArrivedListeners()
    {
    }

    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        _actuatorsSensors = actuatorsSensors;
    }

    @Override
    public void activateDebugging(IVrepDrawing actuatingSensing, DebugParams params)
    {
    }

    @Override
    public void deactivateDebugging()
    {
    }

    @Override
    public void setParameters(Object parameters)
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener)
    {
    }

    @Override
    public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
    {
    }

    @Override
    public void addArrivedListener(IArrivedListener arrivedListener)
    {
    }

    public List<RecordedTrajectoryElement> getTrajectory()
    {
        return _trajectoryRecord;
    }

    @Override
    public void notifySegmentsChanged(List<TrajectoryElement> segments)
    {
        List<Position2D> segmentsToAdd = segments.stream().map(trajectory -> trajectory.getVector().getBase()).collect(Collectors.toList());
        _plannedTrajectory.addAll(segmentsToAdd);
        _plannedTrajectoryToSave.addAll(segmentsToAdd);
        if((_planCnt % _planSaveInterval == 0) && (_plannedTrajectoryToSave == null) && !_plannedTrajectoryToSave.isEmpty())
        {
            _plannedTrajectoryToSaveCopy = new ArrayList<Position2D>();
            _threadPool.execute(_savePlanRunnable);
            _plannedTrajectoryToSave = new ArrayList<Position2D>();
        }
        _planCnt++;
    }
    
    public List<Position2D> getPlannedTrajectory()
    {
        return _plannedTrajectory;
    }

    @Override
    public void notifyRouteChanged(List<Line2D> route)
    {
    }

    @Override
    public void activateRouteDebugging()
    {
    }

    @Override
    public void activateSegmentDebugging()
    {
    }
    
    public void savePermanently()
    {
        try
        {
            _finalMapper.writeValue(new File(_baseOutputDirectory + "TrRe" + _testID + ".json"), _trajectoryRecord);
            _finalMapper.writeValue(new File(_baseOutputDirectory + "Plan" + _testID + ".json"), _plannedTrajectory);
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
