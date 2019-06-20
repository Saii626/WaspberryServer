package app.saikat.WaspberryServer.WebsocketServer;

public class WebsocketConfigurations {

    private int heartbeatInterval;
    private int maxWaitForAuthPacket;
    private int authPacketPollInterval;
    private boolean addDeviceEnabled;

    private WebsocketConfigurations() {}

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public boolean isAddDeviceEnabled() {
        return addDeviceEnabled;
    }

    public int getMaxWaitForAuthPacket() {
        return maxWaitForAuthPacket;
    }

    public int getAuthPacketPollInterval() {
        return authPacketPollInterval;
    }

    public static WebsocketConfigurations getDefault() {
        WebsocketConfigurations configurations = new WebsocketConfigurations();
        configurations.heartbeatInterval = 60;
        configurations.maxWaitForAuthPacket = 60;
        configurations.authPacketPollInterval = 2;
        configurations.addDeviceEnabled = false;
        return configurations;
    }

}