package com.github.jovialen.pioneers.engine.networking.event;

import com.github.jovialen.pioneers.engine.networking.client.Client;
import com.github.jovialen.pioneers.engine.networking.server.Server;

public class ClientDisconnectedEvent extends ServerEvent {
    public Client client;
    public String reason;

    public ClientDisconnectedEvent(Server server, Client client) {
        this(server, client, "No reason given");
    }

    public ClientDisconnectedEvent(Server server, Client client, String reason) {
        super(server);
        this.client = client;
        this.reason = reason;
    }

    public Client getClient() {
        return client;
    }
}
