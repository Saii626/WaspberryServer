package app.saikat.WaspberryServer.WebsocketServer;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;
import app.saikat.WaspberryServer.WebsocketServer.websocket.WebsocketServer;

@Configuration
public class WebsocketServerBeans {

    @Autowired
    private ConfigurationManager configurationManager;
    
    @Bean
    @DependsOn("configurationManager")
    public WebsocketConfigurations websocketConfigurations() throws IOException {
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
