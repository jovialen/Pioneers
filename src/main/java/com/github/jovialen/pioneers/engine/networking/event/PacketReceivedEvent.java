package com.github.jovialen.pioneers.engine.networking.event;

import com.github.jovialen.pioneers.engine.networking.client.Client;
import com.github.jovialen.pioneers.engine.networking.packet.Packet;

public class PacketReceivedEvent {
    public Client client;
    public Packet packet;

    public PacketReceivedEvent(Client client, Packet packet) {
        this.client = client;
        this.packet = packet;
    }

    public Client getClient() {
        return client;
    }

    public Packet getPacket() {
        return packet;
    }
}
