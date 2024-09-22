package com.github.jovialen.engine.ecs.entity;

public record EntityId(long fullId) {
    public EntityId(int id, short generation) {
        this((long) id | ((long) generation << 32));
    }

    public int id() {
        // Lower 32 bytes
        return (int) fullId;
    }

    public short generation() {
        // 48-32th bytes
        return (short) (fullId >> 32);
    }

    public int relation() {
        // Upper 32 bytes
        return (int) (fullId >> 32);
    }

    @Override
    public String toString() {
        return "Id{" + Long.toHexString(fullId) + "}";
    }
}
