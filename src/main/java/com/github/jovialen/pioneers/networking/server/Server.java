package com.github.jovialen.pioneers.networking.server;

import com.github.jovialen.pioneers.networking.client.Client;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final ServerSocket socket;
    private final List<Client> clients = new ArrayList<>();
    private final Thread acceptorThread;

    public Server(int port, Acceptor acceptor) throws IOException {
        Logger.info("Opening server on port {}", port);
        socket = new ServerSocket(port);
        acceptorThread = acceptor.start(this);
    }

    public void close() {
        Logger.info("Closing server {}", this);

        for (Client client : clients) {
            client.disconnect();
        }
        clients.clear();

        try {
            socket.close();
        } catch (IOException e) {
            Logger.warn("Failed to close server: {}", e);
        }

        try {
            acceptorThread.join(Duration.ofMillis(500));
        } catch (InterruptedException e) {
            Logger.warn("Failed to join with server acceptor thread: {}", e);
        }
    }

    public void broadcast(String packet) {
        broadcastExcept(null, packet);
    }

    public void broadcastExcept(Client except, String packet) {
        for (Client client : clients) {
            if (client != except) {
                client.send(packet);
            }
        }
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
