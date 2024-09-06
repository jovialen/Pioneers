package com.github.jovialen.engine.networking.client;

import com.github.jovialen.engine.networking.auth.Authenticator;
import com.github.jovialen.engine.networking.event.PacketReceivedEvent;
import com.github.jovialen.engine.networking.packet.Packet;
import com.github.jovialen.engine.networking.packet.PacketInputStream;
import com.github.jovialen.engine.networking.packet.PacketOutputStream;
import com.google.common.eventbus.EventBus;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Client {
    private final EventBus eventBus;
    private final Socket socket;
    private final PacketInputStream reader;
    private final PacketOutputStream writer;

    private final LinkedBlockingDeque<Packet> incoming = new LinkedBlockingDeque<>();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public Client(EventBus eventBus, Socket socket) throws IOException {
        this.eventBus = eventBus;
        this.socket = socket;
        this.reader = new PacketInputStream(socket.getInputStream());
        this.writer = new PacketOutputStream(socket.getOutputStream());

        primeReadPacket();
    }

    public void disconnect() {
        Logger.info("Disconnecting client {}", this);
        try {
            executor.shutdownNow();
            socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            Logger.warn("Failed to disconnect {}: {}", this, e);
        }
    }

    public void send(Packet packet) {
        Logger.trace("Sending packet {}", packet);
        executor.submit(() -> writePacket(packet));
    }

    public Packet receive() {
        return incoming.poll();
    }

    public Packet receive(Duration timeout) {
        try {
            Logger.trace("Waiting {} nanoseconds to receive packet", timeout.toNanos());
            return incoming.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Logger.warn("Operation timed out while waiting to receive a packet from {}", this);
            return null;
        }
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    public Socket getSocket() {
        return socket;
    }

    public LinkedBlockingDeque<Packet> getIncoming() {
        return incoming;
    }

    @Override
    public String toString() {
        return "Client(" + socket.getRemoteSocketAddress().toString() + ")";
    }

    public static Client connectToServer(String host, int port, EventBus eventBus, Authenticator auth) {
        try {
            Logger.info("Attempting to connect to server at {}:{}", host, port);
            Client client = new Client(eventBus, new Socket(host, port));
            if (auth.authenticateServer(client)) {
                Logger.info("Connection was successful.");
                return client;
            } else {
                Logger.error("Failed to authenticate the client to the server. Connection was refused.");
                client.disconnect();
                return null;
            }
        } catch (IOException e) {
            Logger.error("Failed to connect to server at {}:{}: {}", host, port, e);
            return null;
        }
    }

    private void primeReadPacket() {
        Logger.trace("Priming to read packet from client {}", this);
        executor.submit(this::readPacket);
    }

    private void readPacket() {
        try {
            Packet packet = reader.readPacket();
            incoming.add(packet);

            eventBus.post(new PacketReceivedEvent(this, packet));

            primeReadPacket();
        } catch (IOException e) {
            if (isConnected()) {
                Logger.warn("Failed to read packet from {}: {}", this, e);
                disconnect();
            } else {
                primeReadPacket();
            }
        }
    }

    private void writePacket(Packet packet) {
        try {
            writer.writePacket(packet);
        } catch (IOException e) {
            Logger.error("Failed to send packet through {}: {}", this, e);
            disconnect();
        }
    }
}
