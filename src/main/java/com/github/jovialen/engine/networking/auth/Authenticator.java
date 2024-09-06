package com.github.jovialen.engine.networking.auth;

import com.github.jovialen.engine.networking.client.Client;
import com.github.jovialen.engine.networking.server.Server;

public interface Authenticator {
    boolean authenticateClient(Server server, Client client);
    boolean authenticateServer(Client client);
}
