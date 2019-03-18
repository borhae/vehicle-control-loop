package de.joachim.haensel.phd.scenario.experiment.runner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class RegularSavingRequestListener implements ITrajectoryRequestListener
{
    private ObjectMapper _mapper;
    private String _testID;
    private String _baseDirectory;
    private Map<Long, List<TrajectoryElement>> _configurations;
    private Map<Long, List<TrajectoryElement>> _configurationsInterval;
    private Map<Long, List<TrajectoryElement>> _configurationsIntervalCopy;
    private int _saveInterval;
    private int _notifyCnt;
    private ExecutorService _threadPool;
    private Runnable _saveConfigurationsRunnable;

    public RegularSavingRequestListener(int saveInterval, String baseDirectory, String testID)
    {
        _saveInterval = saveInterval;
        _baseDirectory = baseDirectory;
        _testID = testID;
        _mapper = new ObjectMapper();
        _configurations = new HashMap<>();
        _configurationsInterval = new HashMap<>();
        _threadPool = Executors.newFixedThreadPool(6);
        _saveConfigurationsRunnable = new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    //sorting outsourced to thread so the car thread is not slowed down
                    TreeMap<Long, List<TrajectoryElement>> sortedCopy = new TreeMap<Long, List<TrajectoryElement>>(_configurationsIntervalCopy);
                    _mapper.writeValue(new File(_baseDirectory + "Co" + _testID + "part" + _notifyCnt + ".json"), sortedCopy);
                }
                catch (IOException exc)
                {
                    exc.printStackTrace();
                }
            }
        };
    }

    @Override
    public void notifyNewTrajectories(List<TrajectoryElement> trajectories, long timestamp)
    {
        _configurations.put(Long.valueOf(timestamp), trajectories);
        _configurationsInterval.put(Long.valueOf(timestamp), trajectories);
        if((_notifyCnt % _saveInterval == 0) && (_configurationsInterval != null) && !_configurationsInterval.isEmpty())
        {
            _configurationsIntervalCopy = new HashMap<Long, List<TrajectoryElement>>(_configurationsInterval); // TODO too bad if this is set while the saving thread tries to copy it... :(
            _threadPool.execute(_saveConfigurationsRunnable);
            _configurationsInterval = new HashMap<Long, List<TrajectoryElement>>();
        }
        _notifyCnt++;
    }
    
    public void savePermanently()
    {
        try
        {
            TreeMap<Long, List<TrajectoryElement>> sortedCopy = new TreeMap<Long, List<TrajectoryElement>>(_configurations);
            _mapper.writeValue(new File(_baseDirectory + "Co" + _testID + ".json"), sortedCopy);
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
