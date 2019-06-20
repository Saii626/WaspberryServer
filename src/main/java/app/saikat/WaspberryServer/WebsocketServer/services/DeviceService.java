package app.saikat.WaspberryServer.WebsocketServer.services;

import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.repositories.DeviceRepository;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    private Logger logger;

    public DeviceService(Logger logger) {
        this.logger = logger;
    }

    public Device addDevice(String name, String token) {
        Device device = new Device();
        device.setName(name);
        device.setToken(token);
        return deviceRepository.save(device);
    }

    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    public Device findDevice(String name) {
        return deviceRepository.findByName(name);
    }

    public Device findById(UUID id) {
        return deviceRepository.getOne(id);
    }
}