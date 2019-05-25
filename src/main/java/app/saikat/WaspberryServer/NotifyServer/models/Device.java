package app.saikat.WaspberryServer.NotifyServer.models;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.sql.Timestamp;
import java.time.Instant;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "devices")
public class Device {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "last_pong")
    private Timestamp lastPong;

    public Device() {
        this.id = UUID.randomUUID();
        this.name = null;
        this.createdAt = Timestamp.from(Instant.now());
        this.sessionId = null;
        this.lastPong = Timestamp.from(Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String sessionId() {
        return sessionId;
    }

    public Timestamp getLastPong() {
        return lastPong;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    // public void setCreatedAt(Timestamp createdAt) {
    //     this.createdAt = createdAt;
    // }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setLastPong(Timestamp lastPong) {
        this.lastPong = lastPong;
    }
    
}