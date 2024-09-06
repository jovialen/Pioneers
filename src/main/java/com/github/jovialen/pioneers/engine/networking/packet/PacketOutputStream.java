package com.github.jovialen.pioneers.engine.networking.packet;

import org.tinylog.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PacketOutputStream extends DataOutputStream {
    public PacketOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void writePacket(Packet packet) throws IOException {
        if (packet.getSize() > Packet.MAX_SIZE) {
            Logger.error("Attempting to send a packet larger than the maximum allowed packet size {}.", Packet.MAX_SIZE);
            throw new IllegalStateException("Packet too large");
        }

        writeInt(packet.getSize());
        write(packet.data);
    }
}
