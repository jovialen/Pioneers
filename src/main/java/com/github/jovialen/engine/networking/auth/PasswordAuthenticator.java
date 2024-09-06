package com.github.jovialen.engine.networking.auth;

import com.github.jovialen.engine.networking.client.Client;
import com.github.jovialen.engine.networking.packet.Packet;
import com.github.jovialen.engine.networking.server.Server;
import org.tinylog.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.Duration;
import java.util.Arrays;

public class PasswordAuthenticator implements Authenticator {
    private static final int SALT_LENGTH = 64;
    private static final SecretKeyFactory factory;
    static {
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final String password;

    public PasswordAuthenticator(String password) {
        this.password = password;
    }

    @Override
    public boolean authenticateClient(Server server, Client client) {
        SecureRandom random = new SecureRandom();

        // Generate and send salt
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        client.send(new Packet(salt));

        // Hash password
        byte[] correctHash = hashPassword(salt);
        if (correctHash == null) {
            return false;
        }

        // Receive hash from client
        Packet clientHash = client.receive(Duration.ofMillis(500));
        if (clientHash == null) {
            Logger.error("Client failed to provide a valid response in time");
            return false;
        }
        boolean validAuthentication = Arrays.equals(correctHash, clientHash.data);

        // Inform client of authentication result
        client.send(new Packet(new byte[]{(byte) (validAuthentication ? 1 : 0)}));

        if (!validAuthentication) {
            Logger.error("Client has the wrong password");
        }
        return validAuthentication;
    }

    @Override
    public boolean authenticateServer(Client client) {
        Packet packet;

        // Receive salt
        packet = client.receive(Duration.ofMillis(500));
        if (packet == null) {
            Logger.error("Server did not provide a salt for authentication in time.");
            return false;
        }
        byte[] salt = packet.data;

        // Hash password
        byte[] hash = hashPassword(salt);

        // Send hashed password for confirmation
        client.send(new Packet(hash));

        // Confirm if server authenticated us
        packet = client.receive(Duration.ofMillis(500));
        if (packet == null) {
            Logger.error("Server did not confirm if authentication succeeded. Assuming fail.");
            return false;
        }

        byte[] response = packet.data;
        boolean correctPassword = response.length == 1 && response[0] != 0;

        if (!correctPassword) {
            Logger.error("Incorrect password");
        }
        return correctPassword;
    }

    private byte[] hashPassword(byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            Logger.error("Failed to hash password: {}", e);
            return null;
        }
    }
}
