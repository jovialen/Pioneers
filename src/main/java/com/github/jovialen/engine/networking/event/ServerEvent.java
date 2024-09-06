package com.github.jovialen.engine.networking.event;

import com.github.jovialen.engine.networking.server.Server;

public class ServerEvent {
    public Server server;

    public ServerEvent(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }
}
