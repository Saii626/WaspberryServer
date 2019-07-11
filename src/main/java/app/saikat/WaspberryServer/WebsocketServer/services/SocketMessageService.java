package app.saikat.WaspberryServer.WebsocketServer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.models.SocketMessage;
import app.saikat.WaspberryServer.WebsocketServer.models.SocketMessageDirection;
import app.saikat.WaspberryServer.WebsocketServer.repositories.SocketMessageRepository;

@Service
public class SocketMessageService {

    @Autowired
    private SocketMessageRepository repository;

    public SocketMessage newMessage(Device device, String sessionId, SocketMessageDirection direction, String message) {
        SocketMessage msg = new SocketMessage();
        msg.setDevice(device);
        msg.setSessionId(sessionId);
        msg.setDirection(direction);
        msg.setMessage(message);

        return repository.save(msg);
    }

}