package app.saikat.WaspberryServer.ServerComponents.ErrorHandeling;

import java.util.Date;

public class WaspberryErrorObject {

    private Date time;
    private int statusCode;
    private String task;
    private String errorMessage;

    public WaspberryErrorObject(String task, String msg, int code) {
        this.time = new Date();
        this.statusCode = code;
        this.task = task;
        this.errorMessage = msg;
    }

    public Date getTime() {
        return time;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getTask() {
        return task;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setTask(String task) {
        this.task = task;
    }
}