package de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.NamedObsConf;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoObservationConfiguration;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ThreeDLineDemo extends AbstractAnalysis
{
	private MongoClient _mongoClient;

	public static void main(String[] args) 
	{
		try 
		{
			CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
			MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
			MongoClient mongoClient = MongoClients.create(settings);
			ThreeDLineDemo demo = new ThreeDLineDemo();
			demo.setMongoClient(mongoClient);
			AnalysisLauncher.open(demo);
			
	        mongoClient.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	private void setMongoClient(MongoClient mongoClient) 
	{
		_mongoClient = mongoClient;
	}

	@Override
	public void init() throws Exception 
	{
        MongoDatabase carDatabase = _mongoClient.getDatabase("car");
        List<String> collectionNames = carDatabase.listCollectionNames().into(new ArrayList<String>()).parallelStream().filter(name -> name.startsWith("sim")).collect(Collectors.toList());
        List<MongoCollection<MongoObservationConfiguration>> collections = 
                collectionNames.parallelStream().map(collectionName -> carDatabase.getCollection(collectionName, MongoObservationConfiguration.class)).collect(Collectors.toList());
    
        List<NamedObsConf> decoded = 
                collections.stream().map(collection -> decodeMongoCollection(collection, 100000)).flatMap(list -> list.stream()).collect(Collectors.toList());
        List<NamedObsConf> pruned = decoded.parallelStream().filter(obsConf -> obsConf.getConfiguration().size() == 20).collect(Collectors.toList());

        List<NamedObsConf> aligned = align(pruned).parallelStream().map(obsCon -> simplifyToCity(obsCon)).collect(Collectors.toList());
        
        List<NamedObsConf> luebeckData = aligned.parallelStream().filter(obsCon -> obsCon.getName().equals("Luebeck")).collect(Collectors.toList());

        System.out.println("database read, now getting trajectories");
        Function<NamedObsConf, List<TrajectoryElement>> toTrajectory = entry -> entry.getConfiguration().stream().map(elem -> elem).collect(Collectors.toList());
        List<List<TrajectoryElement>> trajectories = luebeckData.parallelStream().map(toTrajectory).collect(Collectors.toList());
        
        System.out.format("Number of trajectories: %s\n", trajectories.size());
        System.out.println("Computing path coordinates");
        List<List<Coord3d>> paths = 
                trajectories.parallelStream().map(t -> t.stream().map(te -> {return new Coord3d(te.getVector().getbX(), te.getVector().getbY(), te.getVelocity());}).collect(Collectors.toList())).collect(Collectors.toList());
        
        System.out.println("computing lines from coordinates");
        List<LineStrip> lines = paths.parallelStream().map(path -> new LineStrip(path)).collect(Collectors.toList());
        
        System.out.println("assigning colors");
        IntStream.range(0, lines.size()).parallel().forEach(idx -> setColor(lines, idx, lines.size()));
        
        System.out.println("assigning width, display true, showpoints true and wireframe true");
        lines.parallelStream().forEach(line -> {line.setWireframeWidth(1.0f); line.setFaceDisplayed(true); line.setShowPoints(true); line.setWireframeDisplayed(true);});
		
        System.out.println("setting up char");
		chart = AWTChartComponentFactory.chart(Quality.Nicest, "newt");
		System.out.println("adding lines to chart");
		chart.getScene().getGraph().add(lines);
	}

    private void setColor(List<LineStrip> lines, int idx, int maxIdx)
    {
        float hue = ((float)idx)/((float)maxIdx);
        int rgbCompund = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        int red = (rgbCompund >> 16) & 0xFF;
        int green = (rgbCompund >> 8) & 0xFF; 
        int blue = (rgbCompund >> 0) & 0xFF;
        lines.get(idx).setWireframeColor(new Color(red, green, blue, 128));
    }

    private static ArrayList<NamedObsConf> decodeMongoCollection(MongoCollection<MongoObservationConfiguration> collection, int limit)
    {
        return collection.find().limit(limit).map(doc -> new NamedObsConf(doc.decode(), doc.getExperimentID())).into(new ArrayList<NamedObsConf>());
    }
    
    private static List<NamedObsConf> align(List<NamedObsConf> data)
    {
        return data.parallelStream().map(confObs -> (NamedObsConf)TrajectoryNormalizer.normalize(confObs)).collect(Collectors.toList());
    }

    private static NamedObsConf simplifyToCity(NamedObsConf obsCon)
    {
        String name = obsCon.getName();
        if(name.contains("Luebeck"))
        {
            obsCon.setName("Luebeck");
        }
        else if(name.contains("Chandigarh"))
        {
            obsCon.setName("Chandigarh");
        }
        return obsCon;
    }
}
