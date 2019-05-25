package app.saikat.WaspberryServer.NotifyServer;

public class WebsocketConfigurations {

    private int websocketHeartbeatInterval;
    private boolean addDeviceEnabled;

    private WebsocketConfigurations() {}

    public int getWebsocketHeartbeatInterval() {
        return websocketHeartbeatInterval;
    }

    public boolean isAddDeviceEnabled() {
        return addDeviceEnabled;
    }

    public static WebsocketConfigurations getDefault() {
        WebsocketConfigurations configurations = new WebsocketConfigurations();
        configurations.websocketHeartbeatInterval = 60;
        configurations.addDeviceEnabled = false;
        return configurations;
    }

}