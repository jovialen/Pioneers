package com.github.jovialen.pioneers.networking.client;

import com.github.jovialen.pioneers.networking.packet.Packet;
import com.github.jovialen.pioneers.networking.packet.PacketInputStream;
import com.github.jovialen.pioneers.networking.packet.PacketOutputStream;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

public class Client {
    private final Socket socket;
    private final PacketInputStream reader;
    private final PacketOutputStream writer;

    private final LinkedBlockingDeque<Packet> incoming = new LinkedBlockingDeque<>();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public Client(Socket socket) throws IOException {
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
        executor.submit(() -> { writePacket(packet); });
    }

    public Packet receive() {
        return incoming.poll();
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

    public static Client connectToServer(String host, int port) {
        try {
            Logger.info("Attempting to connect to server at {}:{}", host, port);
            Socket socket = new Socket(host, port);
            return new Client(socket);
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
