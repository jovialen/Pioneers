package com.github.jovialen.pioneers.engine.networking.auth;

import com.github.jovialen.pioneers.engine.networking.client.Client;
import com.github.jovialen.pioneers.engine.networking.encoder.ObjectEncoder;
import com.github.jovialen.pioneers.engine.networking.packet.Packet;
import com.github.jovialen.pioneers.engine.networking.server.Server;
import org.tinylog.Logger;

import java.io.Serializable;
import java.time.Duration;

public class VersionAuthenticator implements Authenticator {
    private record Version(long version) implements Serializable {
        public Version(short major, short minor, short patch, short variant) {
            this(((long) major << 48) | ((long) minor << 32) | ((long) patch << 16) | ((long) variant));
        }

        public boolean equals(Version version) {
            System.out.println(version.version);
            System.out.println(this.version);
            return version.version == this.version;
        }
    }

    private final ObjectEncoder encoder;
    private final Version version;

    public VersionAuthenticator(short versionMajor, short versionMinor, short versionPatch) {
        this(versionMajor, versionMinor, versionPatch, (short) 0);
    }

    public VersionAuthenticator(short versionMajor, short versionMinor, short versionPatch, short versionVariant) {
        this.encoder = new ObjectEncoder();
        this.version = new Version(versionMajor, versionMinor, versionPatch, versionVariant);
    }

    @Override
    public boolean authenticateClient(Server server, Client client) {
        // Get the client version
        Packet packet = client.receive(Duration.ofMillis(500));
        if (packet == null) {
            Logger.error("Client failed to provide its version number in time");
            return false;
        }
        Version clientVersion = (Version) encoder.decode(packet)[0];

        // Check if the version matches
        boolean versionMatch = version.equals(clientVersion);

        // Inform client of authentication result
        client.send(new Packet(new byte[]{(byte) (versionMatch ? 1 : 0)}));

        if (!versionMatch) {
            Logger.error("Client is running the wrong version");
        }
        return versionMatch;
    }

    @Override
    public boolean authenticateServer(Client client) {
        client.send(encoder.encode(new Object[]{version}));

        // Confirm if server authenticated us
        Packet packet = client.receive(Duration.ofSeconds(1));
        if (packet == null) {
            Logger.error("Server did not confirm if authentication succeeded. Assuming fail.");
            return false;
        }

        byte[] response = packet.data;
        boolean sameVersion = response.length == 1 && response[0] != 0;

        if (!sameVersion) {
            Logger.error("Client and server are running different versions");
        }
        return sameVersion;
    }
}
