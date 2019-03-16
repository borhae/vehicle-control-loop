package playground;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ReadGroundTruthPieceWise
{
    public static void main(String[] args)
    {
        //specific parsing of trajectory files written by ground truth calculator
        String fileName = args[0];
        Path path = Paths.get(fileName);
        
        JsonFactory mappingFactory = new MappingJsonFactory();
        try
        {
            JsonParser parser = mappingFactory.createParser(path.toFile());
            JsonToken current;
            current = parser.nextToken();
            if(current != JsonToken.START_OBJECT)
            {
                System.out.println("possibly no json file: root should be object");
                return;
            }
            int cnt = 0;
            int maxObj = 100;
            while(parser.nextToken() != JsonToken.END_OBJECT || cnt  > maxObj)
            {
                String idx = parser.getCurrentName();
                System.out.println(idx);
                current = parser.nextToken();
                List<TrajectoryElement> trajectory = new ArrayList<TrajectoryElement>();
                if(current == JsonToken.START_ARRAY)
                {
                    while(parser.nextToken() != JsonToken.END_ARRAY)
                    {
                        TrajectoryElement trajectoryElement = parser.readValueAs(new TypeReference<TrajectoryElement>() {});
                        trajectory.add(trajectoryElement);
                    }
                }
                System.out.println("stop here for now");
                cnt++;
            }
        }
        catch (JsonParseException exc)
        {
            exc.printStackTrace();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
