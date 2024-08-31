package com.github.jovialen.pioneers.networking.server;

import com.github.jovialen.pioneers.networking.client.Client;

public class UnsecureAcceptor extends Acceptor {
    @Override
    public boolean acceptClient(Client client) {
        return true;
    }
}
