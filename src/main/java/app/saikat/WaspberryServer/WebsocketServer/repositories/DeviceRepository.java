package app.saikat.WaspberryServer.WebsocketServer.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.saikat.WaspberryServer.WebsocketServer.models.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Device findByName(String name);

    Device findBySessionId(String sessionId);
}