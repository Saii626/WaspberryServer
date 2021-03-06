package app.saikat.WaspberryServer.WebsocketServer.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.repositories.DeviceRepository;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

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

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
}