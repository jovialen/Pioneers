package com.github.jovialen.pioneers.networking.server;

import com.github.jovialen.pioneers.networking.client.Client;

public class UnsecureAcceptor extends Acceptor {
    @Override
    public boolean authenticateClient(Client client) {
        return true;
    }

    @Override
    public boolean authenticateServer(Client client) {
        return true;
    }
}
