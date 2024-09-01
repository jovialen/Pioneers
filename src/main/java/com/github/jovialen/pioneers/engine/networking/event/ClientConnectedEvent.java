package com.github.jovialen.pioneers.engine.networking.event;

import com.github.jovialen.pioneers.engine.networking.client.Client;
import com.github.jovialen.pioneers.engine.networking.server.Server;

public class ClientConnectedEvent extends ServerEvent {
    public Client client;

    public ClientConnectedEvent(Server server, Client client) {
        super(server);
        this.client = client;
    }

    public Client getClient() {
        return client;
    }
}
