package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class BatchImportData
{
    public static void main(String[] args) throws IOException
    {
        if(args.length != 1)
        {
            System.out.println("need 1 config file containing lines like the following (path id)");
            System.out.println("C:\\dir\\experimentfile.json id");
            return;
        }
        List<String> batchContent = Files.readAllLines(new File(args[0]).toPath());
        Map<String, String> idToPath = batchContent.stream().map(line -> line.split(" ")).collect(Collectors.toMap(splitLine -> splitLine[1], splitLine -> splitLine[0]));
        
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
        MongoClient mongoClient = MongoClients.create(settings);
        ObjectMapper mapper = new ObjectMapper();
        ImportData importer = new ImportData();

        idToPath.entrySet().stream().forEach(entry -> importer.importFileIntoDatabase(new File(entry.getValue()), entry.getKey(), "sim_" + entry.getKey(), mongoClient, mapper));

        mongoClient.close();
    }
}  
