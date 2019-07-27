package app.saikat.WaspberryServer.WebsocketServer.websocket;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import app.saikat.Annotations.ExportUrl;
import app.saikat.Annotations.WaspberryMessageHandler;
import app.saikat.UrlManagement.RequestObjects.AddDevice;
import app.saikat.UrlManagement.ResponseObjects.CreatedDevice;
import app.saikat.UrlManagement.WebsocketMessages.ClientMessages.GetDeviceList;
import app.saikat.UrlManagement.WebsocketMessages.ServerMessages.DeviceList;
import app.saikat.WaspberryServer.ServerComponents.ErrorHandeling.WaspberryErrorException;
import app.saikat.WaspberryServer.WebsocketServer.WebsocketConfigurations;
import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.services.DeviceService;

@RestController
public class SocketController {

    @Autowired
    private DeviceService service;

    @Autowired
    private WebsocketServerLogic websocketServer;

    @Autowired
    private WebsocketConfigurations configurations;

    private Logger logger;

    public SocketController(Logger logger) {
        this.logger = logger;
    }

    @ExportUrl(name = "ADD_DEVICE", url = "/addDevice")
    @PostMapping(name = "/addDevice")
    public ResponseEntity<CreatedDevice> addDevice(@RequestBody AddDevice body) {
        if (configurations.isAddDeviceEnabled()) {
            try {
                Device device = service.addDevice(body.getName(), body.getToken());
                logger.debug("Added device");

                return ResponseEntity.status(HttpStatus.OK).body(new CreatedDevice(device.getId(), device.getName()));
            } catch (Exception e) {
                logger.error("Error: ", e);
                throw new WaspberryErrorException("addDevice", e.getMessage(), HttpStatus.CONFLICT);
            }
        } else {
            logger.error("Not allowed");
            throw new WaspberryErrorException("addDevice", "Not allowed", HttpStatus.FORBIDDEN);
        }
    }

    @WaspberryMessageHandler
    public void getDeviceList(Device sourceDevice, GetDeviceList getDeviceList) {
        List<Device> devices = service.getAllDevices();

        List<CreatedDevice> createdDevices = devices.stream()
                .map(device -> new CreatedDevice(device.getId(), device.getName())).collect(Collectors.toList());
                
        websocketServer.send(sourceDevice, new DeviceList(createdDevices));
    }
    // @PostMapping(name = "/send")
    // public ResponseEntity< send(@RequestBody SendMessage message) {

    // }
}