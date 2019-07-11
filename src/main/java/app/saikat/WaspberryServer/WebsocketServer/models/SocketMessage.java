package app.saikat.WaspberryServer.WebsocketServer.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "socket_messages")
public class SocketMessage {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @OneToOne
    @JoinColumn(name = "id")
    private Device device;

    @Column(name = "direction")
    @Enumerated(EnumType.STRING)
    private SocketMessageDirection direction;

    @Column(name = "received_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date receivedAt = new Date();

    @Column(name = "message")
    private String message;

    @Column(name = "session_id")
    private String sessionId;

    public UUID getId() {
        return id;
    }    

    public Date getReceivedAt() {
        return receivedAt;
    }

    public String getMessage() {
        return message;
    }

    public Device getDevice() {
        return device;
    }
    
    public SocketMessageDirection getDirection() {
        return direction;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }   

    public void setDevice(Device device) {
        this.device = device;
    }
    
    public void setDirection(SocketMessageDirection direction) {
        this.direction = direction;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}