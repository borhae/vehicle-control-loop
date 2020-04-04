package de.joachim.haensel.phd.scenario.experiment.runner.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

import de.joachim.haensel.phd.scenario.experiment.runner.ExperimentConfiguration;

public class ExperimentConfigurationTestLoadSave
{
    public static void main(String[] args)
    {
        ExperimentConfigurationTestLoadSave fuckThat = new ExperimentConfigurationTestLoadSave();
        fuckThat.testSaveConfiguration();
    }
    
    @Test
    public void testSaveConfiguration()
    {
        try
        {
            ExperimentConfiguration config = new ExperimentConfiguration();
            config.setDefault();
            ObjectMapper mapper = new ObjectMapper();
            
            ConfigRenderOptions options = ConfigRenderOptions.defaults().setComments(false).setFormatted(true).setJson(true).setOriginComments(false);
            Config hoconConfig = ConfigFactory.parseString(mapper.writeValueAsString(config));
            String render = hoconConfig.root().render(options);
            Files.writeString(Paths.get("./res/testconfig.cfg"), render, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        } 
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
