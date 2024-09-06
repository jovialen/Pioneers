package com.github.jovialen.engine.networking.server;

import com.github.jovialen.engine.networking.auth.Authenticator;
import com.github.jovialen.engine.networking.auth.UnsecureAuthenticator;
import com.github.jovialen.engine.networking.client.Client;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Acceptor {
    private final Server server;
    private final ThreadPoolExecutor executor;
    private final Authenticator authenticator;

    public Acceptor(Server server) {
        this(server, new UnsecureAuthenticator());
    }

    public Acceptor(Server server, Authenticator authenticator) {
        this(server, authenticator, 1);
    }

    public Acceptor(Server server, Authenticator authenticator, int maxParallelConnectionAttempts) {
        this.server = server;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxParallelConnectionAttempts);
        this.authenticator = authenticator;

        for (int i = 0; i < maxParallelConnectionAttempts; i++) {
            executor.submit(this::waitForClient);
        }
    }

    public void stop() {
        Logger.info("Stopping to accept clients");
        executor.shutdownNow();
    }

    private void waitForClient() {
        try {
            Logger.trace("Waiting for client to connect to {}", server);

            Socket remote = server.getSocket().accept();
            Client connectingClient = new Client(server.getEventBus(), remote);

            executor.submit(() -> authenticateClient(connectingClient));
        } catch (IOException e) {
            Logger.warn("Failed to wait for client to connect to {}: {}", server, e);
        } finally {
            if (server.isOpen()) {
                executor.submit(this::waitForClient);
            }
        }
    }

    private void authenticateClient(Client client) {
        Logger.debug("{} is attempting to connect to the server", client);

        if (authenticator.authenticateClient(server, client)) {
            server.connect(client);
        } else {
            Logger.warn("Client was refused");
            client.disconnect();
        }
    }
}
