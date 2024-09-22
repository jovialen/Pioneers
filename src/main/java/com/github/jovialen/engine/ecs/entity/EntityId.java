package com.github.jovialen.engine.ecs.entity;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityId entityId = (EntityId) o;
        return fullId == entityId.fullId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fullId);
    }

    @Override
    public String toString() {
        return "Id{" + Long.toHexString(fullId) + "}";
    }
}
