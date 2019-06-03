package app.saikat.WaspberryServer.WebsocketServer.websocket;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import app.saikat.WaspberryServer.ServerComponents.ErrorHandeling.WaspberryErrorException;
import app.saikat.WaspberryServer.WebsocketServer.WebsocketConfigurations;
import app.saikat.WaspberryServer.WebsocketServer.models.Device;
import app.saikat.WaspberryServer.WebsocketServer.services.DeviceService;

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
    public ResponseEntity<Device> addDevice(@RequestBody AddDeviceRequestBody body) {
        if (configurations.isAddDeviceEnabled()) {
            try {
                Device device = service.addDevice(body.getName());
                logger.debug("Added device");
                return ResponseEntity.status(HttpStatus.OK).body(device);
            } catch (Exception e) {
                logger.error("Error: ", e);
                throw new WaspberryErrorException("addDevice", e.getMessage(), HttpStatus.CONFLICT);
            }
        } else {
            logger.error("Not allowed");
            throw new WaspberryErrorException("addDevice", "Not allowed", HttpStatus.FORBIDDEN);
        }
    }

}