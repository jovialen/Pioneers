package com.github.jovialen.pioneers.networking.event;

import com.github.jovialen.pioneers.networking.client.Client;
import com.github.jovialen.pioneers.networking.server.Server;

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
