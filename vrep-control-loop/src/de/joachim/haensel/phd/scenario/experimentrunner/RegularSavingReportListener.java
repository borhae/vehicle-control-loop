package de.joachim.haensel.phd.scenario.experimentrunner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;

public class RegularSavingReportListener implements ITrajectoryReportListener
{
    private String _testID;
    private ObjectMapper _mapper;
    private Map<Long, ObservationTuple> _observations;
    private Map<Long, ObservationTuple> _observationsInterval;
    private Map<Long, ObservationTuple> _observationsIntervalCopy;
    private String _baseDirectory;
    private int _saveInterval;
    private int _notifyCnt;
    private ExecutorService _threadPool;
    private Runnable _saveObservationsRunnable;

    public RegularSavingReportListener(int saveInterval, String baseDirectory, String testID)
    {
        _saveInterval = saveInterval;
        _observations = new HashMap<>();
        _observationsInterval = new HashMap<>();
        _mapper = new ObjectMapper();
        _baseDirectory = baseDirectory;
        _testID = testID;
        _notifyCnt = 0;
        _threadPool = Executors.newFixedThreadPool(4);
        _saveObservationsRunnable = new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    //sorting outsourced to thread so the car thread is not slowed down
                    TreeMap<Long, ObservationTuple> sortedCopy = new TreeMap<Long, ObservationTuple>(_observationsIntervalCopy);
                    _mapper.writeValue(new File(_baseDirectory + "Ob" + _testID + "part" + _notifyCnt + ".json"), sortedCopy);
                }
                catch (IOException exc)
                {
                    exc.printStackTrace();
                }
            }
        };
    }

    @Override
    public void notifyEnvironmentState(Position2D rearWheelCP, Position2D frontWheelCP, double[] velocity, long timeStamp)
    {
        ObservationTuple observation = new ObservationTuple(rearWheelCP, frontWheelCP, velocity, timeStamp);
        _observations.put(Long.valueOf(timeStamp), observation);
        _observationsInterval.put(Long.valueOf(timeStamp), observation);
        if((_notifyCnt % _saveInterval == 0) && (_observationsInterval != null) && !_observationsInterval.isEmpty())
        {
            _observationsIntervalCopy = new HashMap<Long, ObservationTuple>(_observationsInterval); // TODO too bad if this is set while the saving thread tries to copy it... :(
            _threadPool.execute(_saveObservationsRunnable);
            _observationsInterval = new HashMap<Long, ObservationTuple>();
        }
        _notifyCnt++;
    }

    public void savePermanently()
    {
        try
        {
            _mapper.writeValue(new File(_baseDirectory + "Ob" + _testID + ".json"), _observations);
        }
        catch (JsonGenerationException exc)
        {
            exc.printStackTrace();
        }
        catch (JsonMappingException exc)
        {
            exc.printStackTrace();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
