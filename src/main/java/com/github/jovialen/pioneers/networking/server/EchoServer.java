package com.github.jovialen.pioneers.networking.server;

import com.github.jovialen.pioneers.networking.client.Client;
import com.github.jovialen.pioneers.networking.encoder.StringEncoder;
import com.github.jovialen.pioneers.networking.packet.Packet;

import java.io.IOException;
import java.time.Duration;

public class EchoServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server(8181, new UnsecureAcceptor());
        StringEncoder encoder = new StringEncoder();

        System.out.println("Waiting for clients to connect to the server");
        while (server.getClients().isEmpty()) {
            if (!server.isOpen()) {
                return;
            }
        }

        System.out.println("Starting server...");
        for (int tick = 0; !server.getClients().isEmpty(); tick++) {
            boolean debugTick = tick % 60 == 0;

            if (debugTick) {
                server.removeDisconnectedClients();
                System.out.println(server.getClients().size() + " clients currently connected");
            }

            Packet packet;
            for (Client client : server.getClients()) {
                if (debugTick) {
                    System.out.println(client.getIncoming().size() + " packets pending from " + client);
                }

                while ((packet = client.receive()) != null) {
                    System.out.println(client + ": " + encoder.decode(packet));
                    server.broadcastExcept(client, packet);
                }
            }

            Thread.sleep(Duration.ofMillis(16));
        }

        server.close();
    }
}
