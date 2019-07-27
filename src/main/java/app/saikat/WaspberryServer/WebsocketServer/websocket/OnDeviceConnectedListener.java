package app.saikat.WaspberryServer.WebsocketServer.websocket;

import app.saikat.WaspberryServer.WebsocketServer.models.Device;

public interface OnDeviceConnectedListener {

    void onDeviceConnected(Device device);
}