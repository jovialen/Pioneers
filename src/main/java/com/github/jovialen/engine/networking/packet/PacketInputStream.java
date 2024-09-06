package com.github.jovialen.engine.networking.packet;

import org.tinylog.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PacketInputStream extends DataInputStream {
    public PacketInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public Packet readPacket() throws IOException {
        int size = readInt();

        if (size > Packet.MAX_SIZE) {
            Logger.error("Receiving a packet larger than maximum allowed packet size {}", Packet.MAX_SIZE);
            throw new IllegalStateException("Packet too large");
        }

        Packet packet = new Packet(size);
        readNBytes(packet.data, 0, size);
        return packet;
    }
}
