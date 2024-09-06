package com.github.jovialen.pioneers.engine.networking.server;

import com.github.jovialen.pioneers.engine.networking.auth.Authenticator;
import com.github.jovialen.pioneers.engine.networking.client.Client;
import com.github.jovialen.pioneers.engine.networking.event.ClientConnectedEvent;
import com.github.jovialen.pioneers.engine.networking.event.ClientDisconnectedEvent;
import com.github.jovialen.pioneers.engine.networking.packet.Packet;
import com.google.common.eventbus.EventBus;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final EventBus eventBus;
    private final ServerSocket socket;
    private final List<Client> clients = new ArrayList<>();
    private final Acceptor acceptor;

    public Server(int port, EventBus eventBus, Authenticator auth) throws IOException {
        Logger.info("Opening server on port {}", port);
        this.eventBus = eventBus;
        this.socket = new ServerSocket(port);
        this.acceptor = new Acceptor(this, auth);
    }

    public void close() {
        Logger.info("Closing server {}", this);

        disconnectAll();
        acceptor.stop();

        try {
            socket.close();
        } catch (IOException e) {
            Logger.warn("Failed to close server: {}", e);
        }
    }

    public void broadcast(Packet packet) {
        broadcastExcept(null, packet);
    }

    public void broadcastExcept(Client except, Packet packet) {
        List<Client> disconnectedClients = new ArrayList<>();

        for (Client client : clients) {
            if (!client.isConnected()) {
                disconnectedClients.add(client);
                continue;
            }

            if (client != except) {
                client.send(packet);
            }
        }

        Logger.debug("Removing disconnected clients");
        disconnectedClients.forEach(this::disconnect);
    }

    public void connect(Client client) {
        Logger.info("{} has connected to the server", client);
        clients.add(client);
        eventBus.post(new ClientConnectedEvent(this, client));
    }

    public void removeDisconnectedClients() {
        List<Client> disconnectedClients = new ArrayList<>();

        for (Client client : clients) {
            if (!client.isConnected()) {
                disconnectedClients.add(client);
            }
        }

        if (!disconnectedClients.isEmpty()) {
            Logger.debug("Removing {} disconnected clients", disconnectedClients.size());
            disconnectedClients.forEach(this::disconnect);
        }
    }

    public void disconnectAll() {
        Logger.info("Disconnecting all clients from the {}", this);
        clients.forEach(Client::disconnect);
        clients.clear();
    }

    public void disconnect(Client client) {
        Logger.info("Disconnecting client {} from {}", client, this);
        client.disconnect();
        clients.remove(client);
        eventBus.post(new ClientDisconnectedEvent(this, client));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public boolean isOpen() {
        return !socket.isClosed();
    }

    public ServerSocket getSocket() {
        return socket;
    }

    public List<Client> getClients() {
        return clients;
    }

    @Override
    public String toString() {
        return "Server(" + socket.getLocalPort() + ")";
    }
}
