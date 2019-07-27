package app.saikat.WaspberryServer.NotifyServer.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.saikat.PojoCollections.WebsocketMessages.ServerMessages.NotificationStatus;
import app.saikat.WaspberryServer.NotifyServer.models.NotifyMessage;

@Repository
public interface NotifyMessageRepository extends JpaRepository<NotifyMessage, UUID> {

    List<NotifyMessage> findByNotificationStatusAndTargetId(NotificationStatus notificationStatus, UUID targetId);

}