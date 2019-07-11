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

@Entity
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastPong(Date lastPong) {
        this.lastPong = lastPong;
    }

    public static Device getAnonymousDevice() {
        Device anonymousDevice = new Device();
        anonymousDevice.name = "Anonymous#" + anonymousDevice.hashCode();
        return anonymousDevice;
    }

    @Override
    public String toString() {
        return String.format("[id: %s, name: %s, token: %s, created_at: %s, last_pong: %s]", id.toString(), name, token,
                createdAt.toString(), lastPong.toString());
    }
}