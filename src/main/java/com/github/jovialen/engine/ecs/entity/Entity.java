package com.github.jovialen.engine.ecs.entity;

import com.github.jovialen.engine.ecs.Register;

import java.util.Objects;

public class Entity {
    private final Register register;
    private final EntityId entityId;

    public Entity(Register register, EntityId entityId) {
        this.register = register;
        this.entityId = entityId;
    }

    public void destroy() {
        register.destroy(entityId);
    }

    public boolean isAlive() {
        return register.checkAlive(entityId);
    }

    public boolean has(Class<?> componentType) {
        return register.hasComponent(entityId, componentType);
    }

    public void set(Object component) {
        register.addComponent(entityId, component);
    }

    public Object get(Class<?> componentType) {
        return register.getComponent(entityId, componentType);
    }

    public void remove(Class<?> componentType) {
        register.removeComponent(entityId, componentType);
    }

    public void setName(String name) {
        register.addComponent(entityId, name);
    }

    public String getName() {
        return (String) register.getComponent(entityId, String.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(register, entity.register) && Objects.equals(entityId, entity.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(register, entityId);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + Integer.toHexString(entityId.id()) +
                ", generation=" + entityId.generation() +
                '}';
    }
}
