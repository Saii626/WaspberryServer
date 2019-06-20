package app.saikat.WaspberryServer.WebsocketServer.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.saikat.WaspberryServer.WebsocketServer.models.SocketMessage;

@Repository
public interface SocketMessageRepository extends JpaRepository<SocketMessage, UUID> {

}
