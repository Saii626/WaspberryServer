package app.saikat.WaspberryServer.ServerComponents;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.saikat.ConfigurationManagement.ConfigurationManagerInstanceHandler;
import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;

@Configuration
public class ServerBeans {

    @Autowired
    private Gson gson;

    @Bean("gson")
    public Gson getGson() {
        return new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .setPrettyPrinting()
        .serializeNulls()
        .create();
    }

    @Bean("configurationManager")
    public ConfigurationManager getConfigurationManager() throws IOException {
        File configFile = new File("ServerConfigurations.txt");
        return ConfigurationManagerInstanceHandler.createInstance(configFile, gson);
    }
}