package app.saikat.WaspberryServer.ServerComponents.ErrorHandeling;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.saikat.PojoCollections.ErrorObjects.WaspberryErrorObject;

@RestControllerAdvice
public class WaspberryErrorController {

    @ExceptionHandler(WaspberryErrorException.class)
    public ResponseEntity<WaspberryErrorObject> handleServerError(WaspberryErrorException exception) {
        WaspberryErrorObject error = exception.getError();

        return ResponseEntity.status(exception.getStatus()).body(error);
    }

}