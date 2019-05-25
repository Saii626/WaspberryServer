package app.saikat.WaspberryServer.NotifyServer;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;

@Configuration
public class NotifyServerBeans {

    @Autowired
    private ConfigurationManager configurationManager;
    
    @Bean
    @DependsOn("configurationManager")
    public WebsocketConfigurations getConf() throws IOException {
        return configurationManager.<WebsocketConfigurations>get("websocket")
            .orElseGet( () -> {
                WebsocketConfigurations conf = WebsocketConfigurations.getDefault();
                configurationManager.put("websocket", conf);
                    try {
                        configurationManager.syncConfigurations();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                return conf;
            });
    }

}
