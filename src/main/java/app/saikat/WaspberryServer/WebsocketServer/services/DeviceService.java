package app.saikat.WaspberryServer.WebsocketServer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.repositories.DeviceRepository;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public Device addDevice(String name) {
        Device device = new Device();
        device.setName(name);
        return deviceRepository.save(device);
    }

    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    public Device findDevice(String name) {
        return deviceRepository.findByName(name);
    }

    public Device findBySessionId(String sessionId) {
        return deviceRepository.findBySessionId(sessionId);
    }
}