package com.github.jovialen.engine.ecs.component;

import com.github.jovialen.engine.ecs.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record Archetype(EntityType type, Table table,
                        Map<Class<?>, ArchetypeEdge> archetypeEdgeMap) {

    public Archetype(EntityType type) {
        this(type, new Table(type.components().size()), new HashMap<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Archetype archetype = (Archetype) o;
        return Objects.equals(type, archetype.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        return "Archetype{" +
                "type=" + type.components() +
                '}';
    }
}
