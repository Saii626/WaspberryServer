package app.saikat.WaspberryServer.NotifyServer.websocket;

import org.springframework.web.socket.WebSocketMessage;

public class WaspberryMessage implements WebSocketMessage<WaspberryMessage> {

    private String type;
    private Object object;

    public WaspberryMessage(String type, Object payload) {

    }

    @Override
    public WaspberryMessage getPayload() {
        return null;
    }

    @Override
    public int getPayloadLength() {
        return 0;
    }

    @Override
    public boolean isLast() {
        return false;
    }

}