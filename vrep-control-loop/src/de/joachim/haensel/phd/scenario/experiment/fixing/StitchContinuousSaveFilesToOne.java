package de.joachim.haensel.phd.scenario.experiment.fixing;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class StitchContinuousSaveFilesToOne
{
    public static void main(String[] args)
    {
        if(args.length < 3)
        {
            System.out.println("usage: StitchContinousSaveFilesToOne <outfilename> <sourceDirectory> <pattern0>.. <pattern n>");
        }
        String outFileName = args[0];
        String rootDirName = args[1];
        String[] inFiles = Arrays.copyOfRange(args, 2, args.length);
        try
        {
            Path rootDir = Paths.get(rootDirName);
            File test = rootDir.toFile();
            if(test.isDirectory())
            {
                System.out.println("this is a directory!");
            }
            else if(test.isDirectory())
            {
                System.out.println("file?");
            }
            else if(test.exists())
            {
                System.out.println("what is this?");
            }
            else
            {
                System.out.println("does not exist");
            }
            TreeMap<Long, List<TrajectoryElement>> result = new TreeMap<Long, List<TrajectoryElement>>();
            ObjectMapper objectMapper = new ObjectMapper();
            for(int idx = 0; idx < inFiles.length; idx++)
            {
                String curFilePattern = inFiles[idx];
                readTrajectoriesForFilePattern(test, curFilePattern, result, objectMapper);
            }
            objectMapper.writeValue(new File(outFileName), result);
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private static void readTrajectoriesForFilePattern(File rootDir, String curFilePattern, TreeMap<Long, List<TrajectoryElement>> result, ObjectMapper objectMapper) throws IOException, JsonParseException, JsonMappingException
    {
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:"+curFilePattern);
        File[] filesInDir = rootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name)
            {
                boolean matchesFile = pathMatcher.matches(Paths.get(name));
                return matchesFile;
            }
        });
        
        Long curOffset = result.isEmpty() ? 0 : result.lastKey();
        System.out.println("last key: " + curOffset);
        for(int idx = 0; idx < filesInDir.length; idx++)
        {
            File file = filesInDir[idx]; 
            if(file.isFile())
            {
                HashMap<Long, List<TrajectoryElement>> readMap = objectMapper.readValue(file, new TypeReference<HashMap<Long, List<TrajectoryElement>>>() {});
                for (Entry<Long, List<TrajectoryElement>> curEntry : readMap.entrySet())
                {
                    Long curKey = curEntry.getKey();
                    Long newKey = curKey + curOffset;
                    result.put(newKey, curEntry.getValue());
                }
            }
            System.out.println(file.toString());
        }
    }

}
