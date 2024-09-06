package com.github.jovialen.engine.networking.encoder;

import com.github.jovialen.engine.networking.packet.Packet;

public abstract class Encoder<T> {
    public Packet encode(T data) {
        if (data == null) {
            return null;
        }
        return encodePacket(data);
    }

    public T decode(Packet packet) {
        if (packet == null) {
            return null;
        }
        return decodePacket(packet);
    }

    protected abstract Packet encodePacket(T data);
    protected abstract T decodePacket(Packet packet);
}
