package com.github.jovialen.pioneers.engine.networking.packet;

public class Packet {
    public byte[] data;

    public Packet(byte[] data) {
        this.data = data;
    }

    public Packet(int size) {
        this.data = new byte[size];
    }

    public int getSize() {
        return this.data.length;
    }
}
