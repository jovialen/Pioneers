package com.github.jovialen.engine.ecs.component;

public class EntityRecord {
    private Archetype archetype;
    private int row;

    public EntityRecord(Archetype archetype, int row) {
        this.archetype = archetype;
        this.row = row;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public void setArchetype(Archetype archetype) {
        this.archetype = archetype;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
