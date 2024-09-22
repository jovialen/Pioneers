package com.github.jovialen.engine.ecs.entity;

import com.github.jovialen.engine.ecs.Register;

public record Entity(Register register, EntityId entityId) {
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
    public String toString() {
        return "Entity{" +
                "id=" + Integer.toHexString(entityId.id()) +
                ", generation=" + entityId.generation() +
                '}';
    }
}
