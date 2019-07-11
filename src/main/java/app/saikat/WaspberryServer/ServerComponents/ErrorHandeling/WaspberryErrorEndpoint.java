package app.saikat.WaspberryServer.ServerComponents.ErrorHandeling;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WaspberryErrorEndpoint {

    private Logger logger;

    public WaspberryErrorEndpoint(Logger logger) {
        this.logger = logger;
    }

    @GetMapping("/error")
    public ResponseEntity<String> getError() {
        logger.error("Error occured");
        return ResponseEntity.ok().body("Error");
    }
}