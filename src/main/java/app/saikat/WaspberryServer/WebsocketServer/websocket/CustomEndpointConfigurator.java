package app.saikat.WaspberryServer.WebsocketServer.websocket;

import org.hibernate.InstantiationException;
import org.springframework.web.socket.server.standard.SpringConfigurator;

public class CustomEndpointConfigurator extends SpringConfigurator {

    private WebsocketServerLogic websocketServer;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (endpointClass.getName().equals(NewWebsocket.class.getName())) {

            if (websocketServer == null) {
                websocketServer = WebsocketServerLogic.getInstance();
            }

            NewWebsocket newWebsocket = new NewWebsocket(websocketServer);
            return (T) newWebsocket;
        } else {
            throw new InstantiationException("Only supported class " + NewWebsocket.class.getSimpleName(),
                    endpointClass);
        }
    }
}