package com.github.jovialen.pioneers.engine.networking.packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PacketInputStream extends DataInputStream {
    public PacketInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public Packet readPacket() throws IOException {
        int size = readInt();
        Packet packet = new Packet(size);
        readNBytes(packet.data, 0, size);
        return packet;
    }
}
