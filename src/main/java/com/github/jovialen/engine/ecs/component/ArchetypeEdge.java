package com.github.jovialen.engine.ecs.component;

public class ArchetypeEdge {
    private Archetype addArchetype;
    private Archetype removeArchetype;

    public Archetype getAddArchetype() {
        return addArchetype;
    }

    public void setAddArchetype(Archetype addArchetype) {
        this.addArchetype = addArchetype;
    }

    public Archetype getRemoveArchetype() {
        return removeArchetype;
    }

    public void setRemoveArchetype(Archetype removeArchetype) {
        this.removeArchetype = removeArchetype;
    }
}
