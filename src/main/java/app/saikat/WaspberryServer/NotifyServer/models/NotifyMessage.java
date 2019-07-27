package app.saikat.WaspberryServer.NotifyServer.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import app.saikat.PojoCollections.WebsocketMessages.ServerMessages.Notification;
import app.saikat.PojoCollections.WebsocketMessages.ServerMessages.NotificationStatus;

@Entity
@Table(name = "notify_messages")
public class NotifyMessage {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Column(name = "source", nullable = true)
    private String source;

    @Column(name = "target_device_id", nullable = false)
    private UUID targetId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Column(name = "ttl", nullable = false)
    private int ttt;

    public NotifyMessage() {
    }

    public NotifyMessage(Notification notification, NotificationStatus status, int ttl, UUID deviceId) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.source = notification.getSource();
        this.timestamp = notification.getTimestamp();
        this.ttt = ttl;
        this.targetId = deviceId;
        this.notificationStatus = status;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public NotificationStatus getStatus() {
        return this.notificationStatus;
    }

    public void setStatus(NotificationStatus status) {
        this.notificationStatus = status;
    }

    public int getTtt() {
        return this.ttt;
    }

    public void setTtt(int ttt) {
        this.ttt = ttt;
    }

    public UUID getTargetId() {
        return this.targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

}