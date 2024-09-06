package com.github.jovialen.pioneers.engine.networking.packet;

public class Packet {
    public static final int MAX_SIZE = 2 * 1024 * 1024; // 2 MB

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
