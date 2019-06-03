package app.saikat.WaspberryServer.WebsocketServer.websocket;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.services.DeviceService;


@Component
public class WebsocketServer implements WebSocketHandler {

    @Autowired
    private DeviceService deviceService;

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final List<WebSocketSession> socketSessions;

    private Thread websocketHeartbeat;

    public WebsocketServer() {
        socketSessions = new ArrayList<>();

        websocketHeartbeat = new Thread(() -> {

            while (true) {
                synchronized (socketSessions) {
                    for (WebSocketSession session : socketSessions) {
                        Device device = deviceService.findBySessionId(session.getId());

                        if (device != null) {
                            try {
                                logger.debug("pinging {}", device.getName());
                                session.sendMessage(new PingMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                                logger.error("Error sending ping to {}", device.getName());
                            }
                        } else {
                            logger.error("How can we have session {} without corrosponding device?", session.getId());
                        }
                    }
                }

                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        websocketHeartbeat.setName("WSHeartBeat");
        websocketHeartbeat.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (socketSessions) {
                for (WebSocketSession session : socketSessions) {
                    logger.debug("Disconnecting session {}", session.getId());
                    try {
                        session.close(new CloseStatus(1001, "Server going down"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        logger.debug("Connected on path {}", uri.getPath());

        String deviceName = uri.getPath().substring(8);
        Device device = deviceService.findDevice(deviceName);

        if (device != null) {
            logger.info("{} connected", deviceName);
            synchronized (socketSessions) {
                socketSessions.add(session);
                device.setSessionId(session.getId());
                device.setLastPong(Timestamp.from(Instant.now()));
                deviceService.saveDevice(device);
            }
        } else {
            session.close(new CloseStatus(1002, "No device found with " + deviceName));
            logger.warn("No device with name {} found", deviceName);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            // handleTextMessage(session, (TextMessage) message);
        } else if (message instanceof BinaryMessage) {
            // handleBinaryMessage(session, (BinaryMessage) message);
        } else if (message instanceof PongMessage) {
            Device device = deviceService.findBySessionId(session.getId());

            if (device != null) {
                device.setLastPong(Timestamp.from(Instant.now()));
                deviceService.saveDevice(device);
                logger.debug("Pong from {}", device.getName());
            } else {
                logger.error("How can we receive pong if no device with {} session exists?", session.getId());
            }
        } else {
            throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error: {}", exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.debug("Closing connection with session {}", session.getId());

        Device device = deviceService.findBySessionId(session.getId());
        if (device != null) {
            logger.info("Closing connection with {}, exitCode {}, reason {}", device.getName(), closeStatus.getCode(),
                    closeStatus.getReason());

            synchronized (socketSessions) {
                socketSessions.remove(session);
                device.setSessionId(null);
                deviceService.saveDevice(device);
            }
        } 
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}