package app.saikat.WaspberryServer.WebsocketServer.unused_files;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import app.saikat.ConfigurationManagement.Gson.JsonObject;
import app.saikat.UrlManagement.WebsocketMessages.Authentication;
import app.saikat.WaspberryServer.WaspberryMessageHandlers;
import app.saikat.WaspberryServer.ServerComponents.ErrorHandeling.WaspberryErrorException;
import app.saikat.WaspberryServer.WebsocketServer.WebsocketConfigurations;
import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.models.SocketMessageDirection;
import app.saikat.WaspberryServer.WebsocketServer.services.DeviceService;
import app.saikat.WaspberryServer.WebsocketServer.services.SocketMessageService;

// @Component
public class WebsocketServer implements WebSocketHandler {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SocketMessageService messageService;

    @Autowired
    private Gson gson;

    @Autowired
    private WebsocketConfigurations configurations;

    @Autowired
    private WaspberryMessageHandlers handlers;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // For authentication packet. Must receive within 60 sec after establishing
    // connection
    private Map<WebSocketSession, Long> awatingAuthPacketMap;
    private Thread authenticationPacketPoller;

    private Thread websocketHeartbeat;
    private Map<Device, WebSocketSession> socketSessionMap;

    private static WebsocketServer instance;

    @PostConstruct
    public void initialize() {

        // Initialize sessionMap and heartbeat and start thread
        socketSessionMap = new HashMap<>();
        websocketHeartbeat = new Thread(() -> {
            while (true) {
                synchronized (this.socketSessionMap) {
                    for (Map.Entry<Device, WebSocketSession> entry : socketSessionMap.entrySet()) {
                        try {
                            logger.debug("pinging {}", entry.getKey().getName());
                            entry.getValue().sendMessage(new PingMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            logger.error("Error sending ping to {}", entry.getKey().getName());
                        }

                    }
                }

                try {
                    Thread.sleep(configurations.getHeartbeatInterval() * 1000);
                } catch (InterruptedException e) {
                    logger.error("Error: ", e);
                }
            }
        });
        websocketHeartbeat.setName("WSHeartBeat");
        websocketHeartbeat.start();

        // Initialize awatingAuthPacketMap and authentication
        awatingAuthPacketMap = new HashMap<>();
        authenticationPacketPoller = new Thread(() -> {
            List<WebSocketSession> sessionsToRemove = new LinkedList<>();
            while (true) {
                sessionsToRemove.clear();

                synchronized (this.awatingAuthPacketMap) {
                    long currentTime = System.currentTimeMillis();
                    logger.debug("Processing started at: {}", currentTime);
                    for (Map.Entry<WebSocketSession, Long> entry : awatingAuthPacketMap.entrySet()) {
                        if (currentTime > (entry.getValue() + configurations.getMaxWaitForAuthPacket())) {
                            try {
                                logger.info("Closing connection to {}. No auth packet received in time", entry, sessionsToRemove);
                                entry.getKey().close(new CloseStatus(1008, "No auth packet received in time"));
                            } catch (IOException e) {
                                logger.error("Error:", e);
                            } finally {
                                // Cannot remove directly. Will invalidate iterator
                                sessionsToRemove.add(entry.getKey());
                            }
                        }
                    }

                    for (WebSocketSession session : sessionsToRemove) {
                        logger.debug("Removing session: {}", session.getId());
                        this.awatingAuthPacketMap.remove(session);
                    }
                }

                try {
                    Thread.sleep(configurations.getAuthPacketPollInterval() * 1000);
                } catch (InterruptedException e) {
                    logger.error("Error: ", e);
                }
            }
        });
        authenticationPacketPoller.setName("WSAuthPoller");
        authenticationPacketPoller.start();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (this.socketSessionMap) {
                for (Map.Entry<Device, WebSocketSession> entry : socketSessionMap.entrySet()) {
                    try {
                        logger.debug("closing connection to {}", entry.getKey().getName());

                        entry.getValue().close(new CloseStatus(1001, "Server going down"));

                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("Error closing connection to {}", entry.getKey().getName());
                    }
                }
            }
        }));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New {} connected. Awaiting authentication packet... ", session.getId());
        synchronized (this.awatingAuthPacketMap) {
            this.awatingAuthPacketMap.put(session, System.currentTimeMillis());
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handleWebsocketMessage(session, (TextMessage) message);
        } else if (message instanceof PongMessage) {
            Device device = getDevice(session);

            if (device != null) {
                device.setLastPong(Timestamp.from(Instant.now()));
                deviceService.saveDevice(device);
                logger.debug("Pong from {}", device.getName());
            } else {
                logger.error("Pong from {}. How??", session.getId());
            }
        } else {
            throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error:", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Device device = getDevice(session);
        if (device != null) {

            logger.info("Connection closed to {}", device.getName());
            synchronized (this.socketSessionMap) {
                socketSessionMap.remove(device);
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleWebsocketMessage(WebSocketSession session, TextMessage message) {

        messageService.newMessage(getDevice(session), session.getId(), SocketMessageDirection.FROM_CLIENT, message.getPayload());

        try {
            JsonObject jsonObject = gson.fromJson(message.getPayload(), JsonObject.class);
            Class<?> objectType = jsonObject.getObject().getClass();
            logger.info("Received {} from {}", objectType.getSimpleName(), session.getId());

            List<WaspberryMessageHandlers.Tuple<Class<?>, String>> handlerList = handlers.getHandlers().get(objectType);
            if (handlerList == null || handlerList.isEmpty()) {
                logger.warn("No handler found for handling websocket message of type {}", objectType.getName());
                return;
            }

            // Loop through handlers and invoke methods
            for (WaspberryMessageHandlers.Tuple<Class<?>, String> entry : handlerList) {
                try {
                    Method method = entry.first.getDeclaredMethod(entry.second);
                    if (method.getParameterCount() == 1) {
                        // - method(Object message)
                        logger.debug("Invoking {}, class: {}", entry.second, entry.first.getSimpleName());
                        method.invoke(null, jsonObject.getObject());

                    } else if (method.getParameterCount() == 2) {
                        if (method.getParameterTypes()[0].getName().equals(WebSocketSession.class.getName())) {
                            // - method(WebSocketSession session, Object message)
                            logger.debug("Invoking {}, class: {}, firstParam: {}", entry.second,
                                    entry.first.getSimpleName(), WebSocketSession.class.getSimpleName());
                            method.invoke(null, session, jsonObject.getObject());

                        } else if (method.getParameterTypes()[0].getName().equals(Device.class.getName())) {
                            // - method(Device device, Object message)
                            logger.debug("Invoking {}, class: {}, firstParam: {}", entry.second,
                                    entry.first.getSimpleName(), Device.class.getSimpleName());
                            method.invoke(null, getDevice(session), jsonObject.getObject());

                        } else {
                            logger.error("Unsupported first parameter. Only {}, and {} are supported",
                                    WebSocketSession.class.getSimpleName(), Device.class.getSimpleName());
                        }
                    } else {
                        logger.error("Unsupported number of parameters");
                    }
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    logger.error("Error: ", e);
                }
            }

        } catch (JsonSyntaxException e) {
            logger.error("Error: ", e);
            sendError(session, "handleWebsocketMessage",
                    "Error while trying to convert message from String to WaspberryMessage", HttpStatus.BAD_REQUEST);
        }

    }

    private void sendError(WebSocketSession session, String task, String message, HttpStatus status) {
        try {
            WaspberryErrorException exception = new WaspberryErrorException(task, message, status);
            send(session, exception);
        } catch (IOException e) {
            logger.error("Error: ", e);
        }
    }

    // Main send function. Creates JsonObject and then sends as gson
    private <T> void send(WebSocketSession session, T object) throws IOException {
        logger.debug("Sending {} to {}", object.getClass().getSimpleName(), session.getId());
        JsonObject jsonObject = new JsonObject(object);
        String message = gson.toJson(jsonObject);
        session.sendMessage(new TextMessage(message));

        messageService.newMessage(getDevice(session), session.getId(), SocketMessageDirection.TO_CLIENT, message);
    }

    private Device getDevice(WebSocketSession session) {
        synchronized (this.socketSessionMap) {
            for (Map.Entry<Device, WebSocketSession> entry : this.socketSessionMap.entrySet()) {
                if (entry.getValue().equals(session)) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    // @WaspberryMessageHandler
    public static void handleAuthenticationMessage(WebSocketSession session, Authentication authObject) {
        if (instance != null) {
            synchronized (instance.awatingAuthPacketMap) {
                instance.awatingAuthPacketMap.remove(session);
            }
            try {
                Device device = instance.deviceService.findById(authObject.getId());
                if (!device.getToken().equals(authObject.getToken())) {
                    throw new EntityNotFoundException("Wrong token");
                }

                synchronized (instance.socketSessionMap) {
                    instance.socketSessionMap.put(device, session);
                }
            } catch (EntityNotFoundException e) {
                instance.logger.error("Error: ", e);
                instance.sendError(session, "handleAuthenticationMessage", "No such device found",
                        HttpStatus.UNAUTHORIZED);
                try {
                    session.close(new CloseStatus(1002, "No such device. Un authorized"));
                } catch (IOException e1) {
                    instance.logger.error("Error: ", e1);
                }
            }
        } else {
            Logger logger = LoggerFactory.getLogger(WebsocketServer.class);
            logger.error("instance null. wtf??");
        }

    }

    // Autowire WebsocketServer to required class and call this function to send
    // object to connected client
    public <T> void send(Device toDevice, T object) throws IOException {
        WebSocketSession session = null;

        synchronized (this.socketSessionMap) {
            session = this.socketSessionMap.get(toDevice);
        }

        if (session != null) {
            send(session, object);
        }
    }
}