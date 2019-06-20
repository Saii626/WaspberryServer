package app.saikat.WaspberryServer.WebsocketServer;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;
import app.saikat.WaspberryServer.WaspberryMessageHandlers;

@Configuration
public class WebsocketServerBeans {

    @Autowired
    private ConfigurationManager configurationManager;
    
    @Bean
    @DependsOn("configurationManager")
    public WebsocketConfigurations websocketConfigurations() throws IOException {
        return configurationManager.getOrSetDefault("websocket", WebsocketConfigurations.getDefault());
    }

    @Bean
    public WaspberryMessageHandlers waspberryMessageHandlers() {
        return new WaspberryMessageHandlers();
    }
}
