package app.saikat.WaspberryServer.NotifyServer;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.saikat.Annotations.WaspberryMessageHandler;
import app.saikat.PojoCollections.CommonObjects.Tuple;
import app.saikat.PojoCollections.WebsocketMessages.ClientMessages.NotifyDevices;
import app.saikat.PojoCollections.WebsocketMessages.ServerMessages.Notification;
import app.saikat.PojoCollections.WebsocketMessages.ServerMessages.NotificationStatus;
import app.saikat.WaspberryServer.NotifyServer.models.NotifyMessage;
import app.saikat.WaspberryServer.NotifyServer.services.NotifyMessageService;
import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.services.DeviceService;
import app.saikat.WaspberryServer.WebsocketServer.websocket.WebsocketServerLogic;

@Component
public class NotifyServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    WebsocketServerLogic websocketServer;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private NotifyMessageService messageService;

    private List<Device> newlyConnectedDevices;
    private Thread notifyThread;

    public NotifyServer() {

        this.newlyConnectedDevices = new LinkedList<>();

        notifyThread = new Thread(() -> {
            while (true) {

                Device newDevice;

                // Remove a device from list and process it
                synchronized (this.newlyConnectedDevices) {
                    while (this.newlyConnectedDevices.isEmpty()) {
                        try {
                            this.newlyConnectedDevices.wait();
                        } catch (InterruptedException e) {
                            logger.error("Error: {}", e);
                        }
                    }

                    newDevice = this.newlyConnectedDevices.get(0);
                    this.newlyConnectedDevices.remove(0);
                }

                if (newDevice == null) {
                    continue;
                }

                logger.debug("Processing: {}", newDevice);
                List<NotifyMessage> unsendMessages = messageService.getMessagesWaitingForDevice(newDevice);

                Calendar currentTime = Calendar.getInstance();

                // Try to send unsent Messages and update message status
                for (NotifyMessage msg : unsendMessages) {
                    if (!websocketServer.isDeviceConnected(newDevice)) {
                        break;
                    }

                    if (msg.getTtt() == -1) {
                        sendAndUpdateMessage(msg, newDevice);
                        continue;
                    }

                    Calendar msgTime = Calendar.getInstance();
                    msgTime.setTime(msg.getTimestamp());
                    msgTime.add(Calendar.SECOND, msg.getTtt());

                    if (msgTime.after(currentTime)) {
                        msg.setStatus(NotificationStatus.TIMED_OUT);
                        messageService.saveMessage(msg);
                    } else {
                        sendAndUpdateMessage(msg, newDevice);
                    }

                    // Dont try to send continuously. Wait for sometime before continuing
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Error: {}", e);
                    }
                }
            }
        });

        notifyThread.setName("pending_notification_thread");
        notifyThread.start();
    }

    @PostConstruct
    public void initialize() {

        this.websocketServer.addOnDeviceConnectedListener(device -> {
            synchronized (this.newlyConnectedDevices) {
                this.newlyConnectedDevices.add(device);
                this.newlyConnectedDevices.notifyAll();
            }
        });
    }

    @WaspberryMessageHandler
    public void notifyDevices(Device device, NotifyDevices notifyDevices) {
        for (Map.Entry<String, Tuple<Notification, Integer>> entry : notifyDevices.getNotificationMap().entrySet()) {
            Device targetDevice = deviceService.findDevice(entry.getKey());

            if (targetDevice != null) {

                NotifyMessage message = new NotifyMessage(entry.getValue().first, NotificationStatus.RECEIVED,
                        entry.getValue().second, targetDevice.getId());
                messageService.saveMessage(message);

                if (websocketServer.isDeviceConnected(targetDevice)) {
                    sendAndUpdateMessage(message, targetDevice);
                } else {
                    if (entry.getValue().second == 0) {
                        message.setStatus(NotificationStatus.TIMED_OUT);
                    } else {
                        message.setStatus(NotificationStatus.WAITING_FOR_DEVICE);
                    }
                    messageService.saveMessage(message);
                }
            } else {
                logger.warn("Device not found");
            }
        }
    }

    private void sendAndUpdateMessage(NotifyMessage message, Device targetDevice) {
        Notification notification = new Notification(message.getId(), message.getTimestamp(), message.getTitle(),
                message.getMessage(), message.getSource());

        boolean result = websocketServer.send(targetDevice, notification);
        message.setStatus(result ? NotificationStatus.SENT : NotificationStatus.FAILED);
        messageService.saveMessage(message);
    }
}