package com.github.jovialen.pioneers.networking.encoder;

import com.github.jovialen.pioneers.networking.packet.Packet;
import org.tinylog.Logger;

import java.io.*;

public class ObjectEncoder extends Encoder<Object[]> {
    private record ObjectPacketHeader(int count) {}

    @Override
    protected Packet encodePacket(Object... data) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(byteArrayOutputStream);
        } catch (IOException e) {
            Logger.error("Failed to create object output stream");
            return null;
        }

        try {
            ObjectPacketHeader header = new ObjectPacketHeader(data.length);
            outputStream.writeObject(header);

            for (Object object : data) {
                outputStream.writeObject(object);
            }
        } catch (IOException e) {
            Logger.error("Failed to encode object");
            return null;
        }

        return new Packet(byteArrayOutputStream.toByteArray());
    }

    @Override
    protected Object[] decodePacket(Packet packet) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(packet.data));

            ObjectPacketHeader header = (ObjectPacketHeader) inputStream.readObject();
            Object[] objects = new Object[header.count];

            for (int i = 0; i < header.count; i++) {
                objects[i] = inputStream.readObject();
            }

            return objects;
        } catch (ClassNotFoundException e) {
            Logger.error("Failed to decode object");
            return null;
        } catch (IOException e) {
            Logger.error("Failed to create object input stream");
            return null;
        }
    }
}
