package app.saikat.WaspberryServer.NotifyServer.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.saikat.PojoCollections.WebsocketMessages.ServerMessages.NotificationStatus;
import app.saikat.WaspberryServer.NotifyServer.models.NotifyMessage;
import app.saikat.WaspberryServer.NotifyServer.repositories.NotifyMessageRepository;
import app.saikat.WaspberryServer.WebsocketServer.models.Device;

@Service
public class NotifyMessageService {

    @Autowired
    private NotifyMessageRepository repository;

    public NotifyMessage getMessage(UUID id) {
        return repository.getOne(id);
    }

    public void saveMessage(NotifyMessage message) {
        repository.save(message);
    }

    public List<NotifyMessage> getMessagesWaitingForDevice(Device device) {
        return repository.findByNotificationStatusAndTargetId(NotificationStatus.WAITING_FOR_DEVICE, device.getId());
    }
}