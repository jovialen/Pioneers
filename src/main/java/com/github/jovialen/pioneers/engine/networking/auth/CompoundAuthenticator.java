package com.github.jovialen.pioneers.engine.networking.auth;

import com.github.jovialen.pioneers.engine.networking.client.Client;
import com.github.jovialen.pioneers.engine.networking.server.Server;

import java.util.List;

public class CompoundAuthenticator implements Authenticator {
    public List<Authenticator> authenticators;

    public CompoundAuthenticator(Authenticator... authenticators) {
        this.authenticators = List.of(authenticators);
    }

    @Override
    public boolean authenticateClient(Server server, Client client) {
        for (Authenticator authenticator : authenticators) {
            if (!authenticator.authenticateClient(server, client)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean authenticateServer(Client client) {
        for (Authenticator authenticator : authenticators) {
            if (!authenticator.authenticateServer(client)) {
                return false;
            }
        }
        return true;
    }
}
