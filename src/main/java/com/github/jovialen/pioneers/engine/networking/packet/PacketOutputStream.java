package com.github.jovialen.pioneers.engine.networking.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PacketOutputStream extends DataOutputStream {
    public PacketOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void writePacket(Packet packet) throws IOException {
        writeInt(packet.getSize());
        write(packet.data);
    }
}
