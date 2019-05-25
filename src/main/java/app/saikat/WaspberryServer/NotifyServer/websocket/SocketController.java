package app.saikat.WaspberryServer.NotifyServer.websocket;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import app.saikat.WaspberryServer.NotifyServer.WebsocketConfigurations;
import app.saikat.WaspberryServer.NotifyServer.models.Device;
import app.saikat.WaspberryServer.NotifyServer.services.DeviceService;

@RestController
public class SocketController {

    @Autowired
    private DeviceService service;

    @Autowired
    private WebsocketConfigurations configurations;

    private Logger logger;

    public SocketController(Logger logger) {
        this.logger = logger;
    }

    @PostMapping(name = "/addDevice")
    public ResponseEntity<Device> addDevice(@RequestBody AddDeviceRequestBody reqBody) {
        if (configurations.isAddDeviceEnabled()) {
            Device device = service.addDevice(reqBody.name);
            logger.debug("Added device");
            return ResponseEntity.status(HttpStatus.OK).body(device);
        } else {
            logger.error("Not allowed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

}