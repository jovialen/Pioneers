package com.github.jovialen.pioneers.networking.client;

import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

public class Client {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private final LinkedBlockingDeque<String> incoming = new LinkedBlockingDeque<>();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);

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

    public void send(String packet) {
        Logger.trace("Sending packet {}", packet);
        executor.submit(() -> { writePacket(packet); });
    }

    public String receive() {
        return incoming.poll();
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    public Socket getSocket() {
        return socket;
    }

    public LinkedBlockingDeque<String> getIncoming() {
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
            String packet = reader.readLine();
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

    private void writePacket(String packet) {
        writer.println(packet);
    }
}
