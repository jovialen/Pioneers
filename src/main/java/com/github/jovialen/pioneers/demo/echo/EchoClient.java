package com.github.jovialen.pioneers.demo.echo;

import com.github.jovialen.pioneers.engine.networking.auth.PasswordAuthenticator;
import com.github.jovialen.pioneers.engine.networking.client.Client;
import com.github.jovialen.pioneers.engine.networking.encoder.StringEncoder;
import com.github.jovialen.pioneers.engine.networking.event.PacketReceivedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EchoClient {
    private final EventBus eventBus;
    private final Client client;
    private final StringEncoder encoder;
    private final BufferedReader reader;

    public EchoClient(String host, int port) throws IOException {
        this.eventBus = new EventBus();
        eventBus.register(this);

        System.out.print("Password: ");
        reader = new BufferedReader(new InputStreamReader(System.in));
        String password = reader.readLine();

        this.encoder = new StringEncoder();
        this.client = Client.connectToServer(host, port, eventBus, new PasswordAuthenticator(password));
    }

    public void disconnect() throws IOException {
        client.disconnect();
        reader.close();
        eventBus.unregister(this);
    }

    public void run() throws IOException {
        while (client.isConnected()) {
            String input = reader.readLine();

            if (input.equals("quit")) {
                break;
            }

            if (!input.isEmpty()) {
                client.send(encoder.encode(input));
            }
        }
    }

    @Subscribe
    public void onPacketReceived(PacketReceivedEvent event) {
        System.out.println(encoder.decode(event.packet));
    }

    public static void main(String[] args) throws IOException {
        EchoClient client = new EchoClient("localhost", 8181);
        client.run();
        client.disconnect();
    }
}
