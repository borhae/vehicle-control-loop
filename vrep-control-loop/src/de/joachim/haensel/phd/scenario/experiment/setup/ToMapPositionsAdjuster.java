package de.joachim.haensel.phd.scenario.experiment.setup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class ToMapPositionsAdjuster 
{
	public static void main(String[] args) 
	{
		RoadMap map = new RoadMap("./res/roadnetworks/chandigarh-roads.net.xml");
		try (Stream<String> stream = Files.lines(Paths.get("./res/roadnetworks/Chandigarhpoints_raw.txt"))) 
		{
			List<Position2D> positions = stream.map(stringPos -> new Position2D(stringPos)).collect(Collectors.toList());
			List<Position2D> adjustedPositions = 
					positions.stream().map(pos -> map.getClosestPointOnMap(pos)).collect(Collectors.toList());
			for(int idx = 0; idx < positions.size(); idx++)
			{
				Position2D curPos = positions.get(idx);
				Position2D curAdjPos = adjustedPositions.get(idx);
				if(!curPos.equals(curAdjPos, 0.1))
				{
					System.out.format("was: (%.2f %.2f), should be: (%.2f, %.2f).", curPos.getX(), curPos.getY(), curAdjPos.getX(), curAdjPos.getY());
					System.out.format("distance: %.4f\n", Position2D.distance(curPos, curAdjPos));
				}
			}
			List<String> lines = adjustedPositions.stream().map(pos -> String.format("%.2f,%.2f", pos.getX(), pos.getY())).collect(Collectors.toList());
			Files.write(Paths.get("./res/roadnetworks/Chandigarhpoints.txt"), lines);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}