package app.saikat.WaspberryServer;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.saikat.ConfigurationManagement.ConfigurationManagerInstanceHandler;
import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;

@Configuration
public class ServerBeans {

    @Bean("gson")
    public Gson getGson() {
        return ConfigurationManagerInstanceHandler.getGson();
    }

    @Bean("configurationManager")
    public ConfigurationManager getConfigurationManager() throws IOException {
        File configFile = new File("ServerConfigurations.txt");
        return ConfigurationManagerInstanceHandler.createInstance(configFile);
    }
}