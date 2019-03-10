package de.joachim.haensel.phd.scenario.experiment.fixing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class StitchContinuousSaveFilesToOne
{
    public static void main(String[] args)
    {
        //specific parsing of trajectory files written by ground truth calculator
        String inFilePattern = args[0];
        String outFileName = args[1];
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + inFilePattern);

        TreeMap<Long, List<TrajectoryElement>> profilesRead = new TreeMap<Long, List<TrajectoryElement>>();
        DirectoryStream<Path> directoryStream;
        try
        {
            directoryStream = Files.newDirectoryStream(Paths.get("."), inFilePattern);
            directoryStream.forEach(path -> appendToProfile(path, profilesRead));
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private static void appendToProfile(Path inPath, TreeMap<Long, List<TrajectoryElement>> profilesRead)
    {
        if(inPath.toFile().isDirectory())
        {
            return;
        }
        
//        Do not need this since they are supposed to be rather small
//        JsonFactory mappingFactory = new MappingJsonFactory();
//        try
//        {
//            JsonParser parser = mappingFactory.createParser(inPath.toFile());
//            JsonToken current;
//            current = parser.nextToken();
//            if(current != JsonToken.START_OBJECT)
//            {
//                System.out.println("possibly no json file: root should be object");
//                return;
//            }
//            while(parser.nextToken() != JsonToken.END_OBJECT)
//            {
//                Long idx = Long.parseLong(parser.getCurrentName());
//                current = parser.nextToken();
//                List<TrajectoryElement> trajectory = new ArrayList<TrajectoryElement>();
//                if(current == JsonToken.START_ARRAY)
//                {
//                    while(parser.nextToken() != JsonToken.END_ARRAY)
//                    {
//                        TrajectoryElement trajectoryElement = parser.readValueAs(new TypeReference<TrajectoryElement>() {});
//                        trajectory.add(trajectoryElement);
//                    }
//                }
//                profilesRead.put(idx, trajectory);
//            }
//        }
//        catch (JsonParseException exc)
//        {
//            exc.printStackTrace();
//        }
//        catch (IOException exc)
//        {
//            exc.printStackTrace();
//        }
    }
}
