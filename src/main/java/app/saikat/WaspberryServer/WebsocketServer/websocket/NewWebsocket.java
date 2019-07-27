package app.saikat.WaspberryServer.WebsocketServer.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/socket", configurator = CustomEndpointConfigurator.class)
public class NewWebsocket {

    private Session session;
    private WebsocketServerLogic websocketServer;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public NewWebsocket(WebsocketServerLogic websocketServer) {
        this.websocketServer = websocketServer;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.debug("Session {} open", session.getId());
        websocketServer.onOpen(session);
    }

    @OnClose
    public void onClose() {
        logger.debug("Session {} closed", session.getId());
        websocketServer.onClose(session);
    }

    @OnMessage
    public void onMessage(String message) {
        logger.debug("Session {} meessage {}", session.getId(), message);
        websocketServer.onMessage(message, session);
    }

    @OnMessage
    public void onPongMessage(PongMessage message) {
        logger.debug("Session {} ponged", session.getId());
        websocketServer.onPongMessage(message, session);
    }

    @OnError
    public void onError(Throwable error) {
        logger.debug("Session {} errored", session.getId());
        websocketServer.onError(error, session);
    }
}