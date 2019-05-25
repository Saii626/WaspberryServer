package app.saikat.WaspberryServer.NotifyServer.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.saikat.WaspberryServer.NotifyServer.models.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Device findByName(String name);

    Device findBySessionId(String sessionId);
}