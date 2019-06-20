package app.saikat.WaspberryServer.WebsocketServer.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;

import java.sql.Timestamp;

@Entity
@DynamicUpdate
@Table(name = "devices")
public class Device {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "name", unique = true, nullable = false)
    @NotNull
    private String name;

    @Column(name = "created_at", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Column(name = "token")
    private String token;

    @Column(name = "last_pong")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPong;

    // public Device() {
    //     this.id = UUID.randomUUID();
    //     this.name = null;
    //     this.createdAt = Timestamp.from(Instant.now());
    //     this.sessionId = null;
    //     this.lastPong = Timestamp.from(Instant.now());
    // }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public Date getLastPong() {
        return lastPong;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLastPong(Timestamp lastPong) {
        this.lastPong = lastPong;
    }
    
}