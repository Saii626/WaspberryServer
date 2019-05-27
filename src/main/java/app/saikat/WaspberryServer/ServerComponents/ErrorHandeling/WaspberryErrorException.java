package app.saikat.WaspberryServer.ServerComponents.ErrorHandeling;

import org.springframework.http.HttpStatus;

public class WaspberryErrorException extends RuntimeException {

    private static final long serialVersionUID = -6520421759403740921L;

    private WaspberryErrorObject errorObj;
    private HttpStatus status;

    public WaspberryErrorException(String task, String reason, HttpStatus status) {
        super(reason);
        
        this.errorObj = new WaspberryErrorObject(task, reason, status.value());
        this.status = status;
    }

    public WaspberryErrorObject getError() {
        return errorObj;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
}