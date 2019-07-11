package app.saikat.WaspberryServer.WebsocketServer.unused_files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.server.standard.SpringConfigurator;

public class CustomEndpointConfigurator extends SpringConfigurator {

    @Autowired
    private ApplicationContext context;

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return context.getBean(endpointClass);
    }
}