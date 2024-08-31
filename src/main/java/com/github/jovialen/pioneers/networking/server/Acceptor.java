package com.github.jovialen.pioneers.networking.server;

import com.github.jovialen.pioneers.networking.client.Client;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.Socket;

public abstract class Acceptor {
    public Thread start(Server server) {
        Thread thread = new Thread(() -> { run(server); });
        thread.start();
        return thread;
    }

    public void run(Server server) {
        while (server.isOpen()) {
            try {
                Logger.trace("Waiting for client to connect to {}", server);
                Socket remote = server.getSocket().accept();
                Client connectingClient = new Client(remote);

                Logger.debug("{} is attempting to connect to the server", connectingClient);
                if (acceptClient(connectingClient)) {
                    Logger.info("{} has connected to the server", connectingClient);
                    server.getClients().add(connectingClient);
                } else {
                    Logger.warn("Client was refused");
                }
            } catch (IOException e) {
                Logger.warn("Failed to accept client for server {}: {}", server, e);
            }
        }
        Logger.debug("No longer waiting for clients to connect to the server");
    }

    public abstract boolean acceptClient(Client client);
}
