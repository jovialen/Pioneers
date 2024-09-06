package com.github.jovialen.engine.networking.encoder;

import com.github.jovialen.engine.networking.packet.Packet;

import java.nio.charset.StandardCharsets;

public class StringEncoder extends Encoder<String> {
    @Override
    protected Packet encodePacket(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        return new Packet(bytes);
    }

    @Override
    protected String decodePacket(Packet packet) {
        return new String(packet.data, StandardCharsets.UTF_8);
    }
}
