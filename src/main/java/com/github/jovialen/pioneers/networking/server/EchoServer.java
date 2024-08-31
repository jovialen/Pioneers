package com.github.jovialen.pioneers.networking.server;

import com.github.jovialen.pioneers.networking.client.Client;

import java.io.IOException;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        Server server = new Server(8181, new UnsecureAcceptor());

        System.out.println("Starting server...");
        while (!server.getClients().isEmpty()) {
            String packet;
            for (Client client : server.getClients()) {
                while ((packet = client.receive()) != null) {
                    System.out.println(client + ": " + packet);
                    server.broadcastExcept(client, packet);
                }
            }
        }

        server.close();
    }
}
