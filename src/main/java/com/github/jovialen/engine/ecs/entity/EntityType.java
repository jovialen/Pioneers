package com.github.jovialen.engine.ecs.entity;

import java.util.Objects;
import java.util.Set;

public record EntityType(Set<Class<?>> components) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityType that = (EntityType) o;
        return Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(components);
    }

    @Override
    public String toString() {
        return "EntityType{" +
                "components=" + components +
                '}';
    }
}
