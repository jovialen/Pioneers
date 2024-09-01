package com.github.jovialen.pioneers.demo.echo;

import com.github.jovialen.pioneers.engine.networking.auth.PasswordAuthenticator;
import com.github.jovialen.pioneers.engine.networking.encoder.StringEncoder;
import com.github.jovialen.pioneers.engine.networking.event.ClientConnectedEvent;
import com.github.jovialen.pioneers.engine.networking.event.ClientDisconnectedEvent;
import com.github.jovialen.pioneers.engine.networking.event.PacketReceivedEvent;
import com.github.jovialen.pioneers.engine.networking.server.Server;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

public class EchoServer {
    private final EventBus eventBus;
    private final Server server;
    private final StringEncoder encoder;

    public EchoServer() throws IOException {
        eventBus = new EventBus();
        eventBus.register(this);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Server password: ");
        String password = reader.readLine();
        Logger.info("Using password: {}", password);

        server = new Server(8181, eventBus, new PasswordAuthenticator(password));
        encoder = new StringEncoder();
    }

    public void close() {
        server.close();
        eventBus.unregister(this);
    }

    public void run() throws InterruptedException {
        Logger.info("Starting server...");
        for (int tick = 0; !server.getClients().isEmpty(); tick++) {
            boolean debugTick = tick % 60 == 0;

            if (debugTick) {
                server.removeDisconnectedClients();
                System.out.println(server.getClients().size() + " clients currently connected");
            }

            Thread.sleep(Duration.ofMillis(16));
        }
    }

    public void waitForClient() {
        Logger.info("Waiting for clients to connect to the server");
        while (server.getClients().isEmpty()) {
            if (!server.isOpen()) {
                return;
            }
        }
    }

    @Subscribe
    public void onClientConnect(ClientConnectedEvent event) {
        server.broadcastExcept(event.getClient(), encoder.encode("A new client has connected to the server"));
    }

    @Subscribe
    public void onClientDisconnect(ClientDisconnectedEvent event) {
        server.broadcast(encoder.encode("A client has disconnected from the server"));
    }

    @Subscribe
    public void onPacketReceived(PacketReceivedEvent event) {
        server.broadcastExcept(event.getClient(), event.getPacket());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        EchoServer echoServer = new EchoServer();
        echoServer.waitForClient();
        echoServer.run();
        echoServer.close();
    }
}
