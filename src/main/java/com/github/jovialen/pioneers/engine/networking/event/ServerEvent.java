package com.github.jovialen.pioneers.engine.networking.event;

import com.github.jovialen.pioneers.engine.networking.server.Server;

public class ServerEvent {
    public Server server;

    public ServerEvent(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }
}
