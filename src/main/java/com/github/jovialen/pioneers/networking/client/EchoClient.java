package com.github.jovialen.pioneers.networking.client;

import com.github.jovialen.pioneers.networking.encoder.StringEncoder;
import com.github.jovialen.pioneers.networking.server.SecureAcceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        Client client = Client.connectToServer("localhost", 8181, new SecureAcceptor("password"));
        StringEncoder encoder = new StringEncoder();

        if (client == null) {
            System.err.println("Failed to create client");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (client.isConnected()) {
            String line = reader.readLine();

            if (line.equals("quit")) {
                break;
            }

            if (!line.isEmpty()) {
                client.send(encoder.encode(line));
            }

            String received;
            while ((received = encoder.decode(client.receive())) != null) {
                System.out.println(received);
            }
        }

        reader.close();
        client.disconnect();
    }
}
