package com.github.jovialen.engine.ecs.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityRegister {
    private int nextId = 0;
    private final Set<Integer> reusableIds = new HashSet<>();
    private final Map<Integer, Short> generations = new HashMap<>();

    public EntityId create() {
        int id = getUnusedId();
        short generation = makeNewGeneration(id);
        return new EntityId(id, generation);
    }

    public void destroy(EntityId entityId) {
        destroy(entityId.id());
    }

    public void destroy(int id) {
        makeIdReusable(id);
    }

    public boolean isRegistered(EntityId entityId) {
        // Check that both the id is registered, and that the id is still in the same generation
        return isRegistered(entityId.id()) && generations.get(entityId.id()) == entityId.generation();
    }

    public boolean isRegistered(int id) {
        // Just check that the id is registered. We don't care about the generation of the id here.
        return id < nextId && !reusableIds.contains(id);
    }

    private int getUnusedId() {
        // Check if there are any reusable ids
        if (!reusableIds.isEmpty()) {
            int id = reusableIds.iterator().next();
            reusableIds.remove(id);
            return id;
        }

        // Create a new id
        return nextId++;
    }

    private void makeIdReusable(int id) {
        reusableIds.add(id);
    }

    private short makeNewGeneration(int id) {
        // Get generation
        short generation = (short) (generations.getOrDefault(id, (short) 0) + 1);

        // Progress generation
        generations.put(id, generation);

        // Return generation
        return generation;
    }
}
