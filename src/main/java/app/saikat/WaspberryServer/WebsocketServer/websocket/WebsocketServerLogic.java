package app.saikat.WaspberryServer.WebsocketServer.websocket;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.CloseReason.CloseCodes;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import app.saikat.Annotations.WaspberryMessageHandler;
import app.saikat.ConfigurationManagement.Gson.JsonObject;
import app.saikat.PojoCollections.CommonObjects.Status;
import app.saikat.PojoCollections.CommonObjects.Tuple;
import app.saikat.PojoCollections.ErrorObjects.WaspberryErrorObject;
import app.saikat.PojoCollections.WebsocketMessages.ClientMessages.Authentication;
import app.saikat.PojoCollections.WebsocketMessages.ServerMessages.AuthenticationResponse;
import app.saikat.WaspberryServer.WaspberryMessageHandlers;
import app.saikat.WaspberryServer.WebsocketServer.WebsocketConfigurations;
import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.models.SocketMessageDirection;
import app.saikat.WaspberryServer.WebsocketServer.services.DeviceService;
import app.saikat.WaspberryServer.WebsocketServer.services.SocketMessageService;

@Component
public class WebsocketServerLogic {
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

    @Autowired
    private ApplicationContext context;

    // For authentication packet. Must receive within 60 sec after establishing
    // connection
    private Map<Session, Long> awatingAuthPacketMap;
    private Thread authenticationPacketPoller;

    private Thread websocketHeartbeat;
    private BiMap<Device, Session> socketSessionMap;

    private List<WeakReference<OnDeviceConnectedListener>> deviceConnectedObservers;

    private Logger logger = LoggerFactory.getLogger(NewWebsocket.class);

    private static WebsocketServerLogic instance;

    public WebsocketServerLogic() {
        instance = this;

        deviceConnectedObservers = new LinkedList<>();
    }

    public static WebsocketServerLogic getInstance() {
        return instance;
    }

    @PostConstruct
    public void initWebsocketServer() {
        // Initialize sessionMap and heartbeat and start thread
        socketSessionMap = HashBiMap.create();
        websocketHeartbeat = new Thread(() -> {
            while (true) {
                synchronized (this.socketSessionMap) {
                    for (Map.Entry<Device, Session> entry : socketSessionMap.entrySet()) {
                        try {
                            logger.trace("pinging {}", entry.getKey().getName());
                            entry.getValue().getAsyncRemote()
                                    .sendPing(ByteBuffer.wrap(Instant.now().toString().getBytes()));
                        } catch (IOException e) {
                            logger.error("Error: ", e);
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
            List<Session> sessionsToRemove = new LinkedList<>();
            while (true) {
                sessionsToRemove.clear();

                synchronized (this.awatingAuthPacketMap) {
                    long currentTime = System.currentTimeMillis();
                    logger.trace("Processing started at: {}", currentTime);

                    for (Map.Entry<Session, Long> entry : awatingAuthPacketMap.entrySet()) {
                        if (currentTime > (entry.getValue() + configurations.getMaxWaitForAuthPacket())) {
                            try {
                                logger.info("Closing connection to {}. No auth packet received in time", entry,
                                        sessionsToRemove);
                                entry.getKey().close(new CloseReason(CloseCodes.VIOLATED_POLICY,
                                        "Auth packet not received in time"));
                            } catch (IOException e) {
                                logger.error("Error:", e);
                            } finally {
                                // Cannot remove directly. Will invalidate iterator
                                sessionsToRemove.add(entry.getKey());
                            }
                        }
                    }

                    for (Session session : sessionsToRemove) {
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
                for (Map.Entry<Device, Session> entry : socketSessionMap.entrySet()) {
                    try {
                        logger.debug("closing connection to {}", entry.getKey().getName());

                        entry.getValue().close(new CloseReason(CloseCodes.GOING_AWAY, "Server shuttig down"));

                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("Error closing connection to {}", entry.getKey().getName());
                    }
                }
            }
        }));

    }

    private void sendError(Session session, String task, String message, HttpStatus status) {
        try {
            WaspberryErrorObject error = new WaspberryErrorObject(task, message, status.value());
            send(session, error);
        } catch (IOException e) {
            logger.error("Error: ", e);
        }
    }

    // Main send function. Creates JsonObject and then sends as gson
    private <T> void send(Session session, T object) throws IOException {
        logger.debug("Sending {} to {}", object.getClass().getSimpleName(), session.getId());
        JsonObject jsonObject = new JsonObject(object);
        String message = gson.toJson(jsonObject);
        session.getBasicRemote().sendText(message);

        Device device;
        synchronized (this.socketSessionMap) {
            device = this.socketSessionMap.inverse().get(session);
        }

        messageService.newMessage(device, session.getId(), SocketMessageDirection.TO_CLIENT, message);
    }

    @WaspberryMessageHandler
    public void handleAuthenticationMessage(Session session, Authentication authObject) {

        logger.info("Handling Authentication from {}", session.getId());
        synchronized (awatingAuthPacketMap) {
            awatingAuthPacketMap.remove(session);
        }
        try {
            Device device = deviceService.findById(authObject.getId());
            if (device.getToken() != null && !device.getToken().equals(authObject.getToken())) {
                throw new EntityNotFoundException("Wrong token");
            }

            device.setLastPong(new Date());
            synchronized (this.socketSessionMap) {
                this.socketSessionMap.forcePut(device, session);
                AuthenticationResponse response = new AuthenticationResponse(Status.SUCCESS, null);
                try {
                    send(session, response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            synchronized (this.deviceConnectedObservers) {
                ListIterator<WeakReference<OnDeviceConnectedListener>> it = this.deviceConnectedObservers.listIterator();

                while(it.hasNext()) {
                    WeakReference<OnDeviceConnectedListener> weakListener = it.next();
                    if (weakListener.get() == null) {
                        it.remove();
                    } else {
                        OnDeviceConnectedListener listener = weakListener.get();
                        listener.onDeviceConnected(device);
                    }
                }
            }
        } catch (EntityNotFoundException e) {
            logger.error("Error: ", e);
            try {

                AuthenticationResponse response = new AuthenticationResponse(Status.FAILED, null);
                send(session, response);
                session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "No such device. Un authorized"));
            } catch (IOException e1) {
                logger.error("Error: ", e1);
            }
        }
    }

    // Autowire WebsocketServer to required class and call this function to send
    // object to connected client
    // a == b || (a != null && a.equals(b))
    public <T> boolean send(Device toDevice, T object) {
        Session session = null;

        synchronized (this.socketSessionMap) {
            session = this.socketSessionMap.get(toDevice);

            if (session != null) {
                try {
                    send(session, object);
                    return true;
                } catch (IOException e) {
                    logger.error("Error: {}", e);
                }
            } else {
                logger.error("No such device in sessionMap");
            }

            return false;
        }
    }

    public boolean isDeviceConnected(Device device) {
        synchronized (this.socketSessionMap) {
            return this.socketSessionMap.containsKey(device);
        }
    }

    public void onOpen(Session session) {
        logger.info("New {} connected. Awaiting authentication packet... ", session.getId());
        synchronized (this.socketSessionMap) {
            this.socketSessionMap.put(Device.getAnonymousDevice(), session);
            synchronized (this.awatingAuthPacketMap) {
                this.awatingAuthPacketMap.put(session, System.currentTimeMillis());
            }
        }
    }

    public void onClose(Session session) {

        synchronized (this.socketSessionMap) {
            Device device = this.socketSessionMap.inverse().get(session);
            String id = (device != null) ? device.getName() : session.getId();
            logger.info("Connection closed to {}", id);
            socketSessionMap.remove(device);
        }
    }

    public void onMessage(String message, Session session) {

        Device device;
        synchronized (this.socketSessionMap) {
            device = this.socketSessionMap.inverse().get(session);
            if (device == null) {
                logger.error("device not in session map");
                return;
            }
        }

        messageService.newMessage(device, session.getId(), SocketMessageDirection.FROM_CLIENT, message);

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            Class<?> objectType = jsonObject.getObject().getClass();
            logger.info("Received {} from {}", objectType.getSimpleName(), session.getId());

            List<Tuple<Class<?>, String>> handlerList = handlers.getHandlers().get(objectType);
            if (handlerList == null || handlerList.isEmpty()) {
                logger.warn("No handler found for handling websocket message of type {}", objectType.getName());
                return;
            }

            // Loop through handlers and invoke methods
            for (Tuple<Class<?>, String> entry : handlerList) {
                try {

                    Object obj = context.getBean(entry.first);
                    try {
                        Method method = entry.first.getDeclaredMethod(entry.second, objectType);
                        // - method(Object message)
                        logger.debug("Invoking {}, class: {}, withObj: {}", entry.second, entry.first.getSimpleName(),
                                obj);
                        method.invoke(obj, jsonObject.getObject());
                        return;
                    } catch (NoSuchMethodException e) {
                    }

                    try {
                        Method method = entry.first.getDeclaredMethod(entry.second, Session.class, objectType);
                        // - method(WebSocketSession session, Object message)
                        logger.debug("Invoking {}, class: {}, firstParam: {}, secondParam: {}, withObj: {}",
                                entry.second, entry.first.getSimpleName(), Session.class.getSimpleName(),
                                objectType.getSimpleName(), obj);
                        method.invoke(obj, session, jsonObject.getObject());
                        return;
                    } catch (NoSuchMethodException e) {
                    }

                    try {
                        Method method = entry.first.getDeclaredMethod(entry.second, Device.class, objectType);
                        // - method(Device device, Object message)
                        logger.debug("Invoking {}, class: {}, firstParam: {}, secondParam: {}, withObj: {}",
                                entry.second, entry.first.getSimpleName(), Device.class.getSimpleName(),
                                objectType.getSimpleName(), obj);
                        method.invoke(obj, device, jsonObject.getObject());
                        return;
                    } catch (NoSuchMethodError e) {

                        logger.error("No appropriate websocket \"{}\" method found for {} in {}", entry.second,
                                objectType.getSimpleName(), entry.first.getSimpleName());
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

    public void onPongMessage(PongMessage message, Session session) {
        Device device;
        synchronized (this.socketSessionMap) {
            device = this.socketSessionMap.inverse().get(session);
        }

        if (device != null) {
            device.setLastPong(Timestamp.from(Instant.now()));
            deviceService.saveDevice(device);
            logger.debug("Pong from {}", device.getName());
        } else {
            logger.error("Pong from {}. How??", session.getId());
        }
    }

    public void onError(Throwable error, Session session) {
        logger.error("Error in " + session.getId(), error);
    }

    public void addOnDeviceConnectedListener(OnDeviceConnectedListener listener) {
        synchronized (this.deviceConnectedObservers) {
            this.deviceConnectedObservers.add(new WeakReference<>(listener));
        }
    }
}