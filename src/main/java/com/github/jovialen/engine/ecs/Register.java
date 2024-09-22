package com.github.jovialen.engine.ecs;

import com.github.jovialen.engine.ecs.component.*;
import com.github.jovialen.engine.ecs.entity.Entity;
import com.github.jovialen.engine.ecs.entity.EntityId;
import com.github.jovialen.engine.ecs.entity.EntityRegister;
import com.github.jovialen.engine.ecs.entity.EntityType;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Register {
    public final static TaggedLogger LOGGER = Logger.tag("ecs");

    private final Archetype EMPTY_ARCHETYPE = new Archetype(new EntityType(new HashSet<>()));

    private final EntityRegister entityRegister = new EntityRegister();
    private final Map<EntityId, EntityRecord> entityMap = new HashMap<>();
    private final Map<EntityType, Archetype> archetypeMap = new HashMap<>();
    private final Map<Class<?>, Map<Archetype, ArchetypeRecord>> componentMap = new HashMap<>();

    public EntityId create() {
        EntityId entityId = entityRegister.create();
        registerEntity(entityId);
        return entityId;
    }

    public Entity createEntity() {
        return new Entity(this, create());
    }

    public void destroy(EntityId entityId) {
        unregisterEntity(entityId);
        entityRegister.destroy(entityId);
    }

    public void destroyEntity(Entity entity) {
        // Just in case the entity originates from a different register.
        entity.destroy();
    }

    public boolean checkAlive(EntityId entityId) {
        return entityRegister.isRegistered(entityId);
    }

    public boolean hasComponent(EntityId entityId, Class<?> componentType) {
        // Get the archetype of the entity
        EntityRecord entityRecord = entityMap.get(entityId);
        if (entityRecord == null) {
            return false;
        }

        // Get all archetypes which contains the component
        Map<Archetype, ArchetypeRecord> archetypes = componentMap.get(componentType);
        if (archetypes == null) {
            return false;
        }

        // Check if any of the archetypes with the components is the archetype of the entity
        return archetypes.containsKey(entityRecord.getArchetype());
    }

    public Object getComponent(EntityId entityId, Class<?> componentType) {
        // Get the archetype of the entity
        EntityRecord entityRecord = entityMap.get(entityId);
        if (entityRecord == null) {
            return null;
        }
        Archetype archetype = entityRecord.getArchetype();

        // Get all archetypes which contains the component
        Map<Archetype, ArchetypeRecord> archetypes = componentMap.get(componentType);
        if (archetypes == null) {
            return null;
        }

        // Check if the entity archetype has the component
        if (!archetypes.containsKey(archetype)) {
            return null;
        }

        // Get the column of the component in the archetype table
        ArchetypeRecord archetypeRecord = archetypes.get(archetype);

        // Get the component
        return archetype.table().get(archetypeRecord.column(), entityRecord.getRow());
    }

    public void addComponent(EntityId entityId, Object component) {
        LOGGER.trace("Adding {} to {}", component, entityId);

        // Get the current archetype of the entity
        EntityRecord entityRecord = entityMap.get(entityId);
        if (entityRecord == null) {
            throw new RuntimeException("Cannot add component to entity. Entity is not in the register.");
        }
        Archetype archetype = entityRecord.getArchetype();

        // Get all archetypes which contain the component
        Class<?> componentType = component.getClass();
        registerComponent(componentType);

        Map<Archetype, ArchetypeRecord> archetypes = componentMap.get(componentType);
        if (archetypes == null) {
            throw new RuntimeException("Cannot add component to entity. Component is not in the register.");
        }

        // Check if the archetype doesn't have the component
        if (!archetypes.containsKey(archetype)) {
            // If so...

            // Get the next archetype of the entity
            Archetype nextArchetype = getAddArchetype(archetype, componentType);

            // Move the entity into its new archetype
            int row = moveEntity(archetype, entityRecord.getRow(), nextArchetype);
            entityRecord.setArchetype(nextArchetype);
            entityRecord.setRow(row);

            // And continue with the new archetype
            archetype = nextArchetype;
        }


        // Get the column of the component in the new archetype
        ArchetypeRecord archetypeRecord = archetypes.get(archetype);
        if (archetypeRecord == null) {
            throw new RuntimeException("Cannot add component to entity. Archetype is not in the register.");
        }

        // Store the new component in the archetype table
        archetype.table().set(archetypeRecord.column(), entityRecord.getRow(), component);
    }

    public void removeComponent(EntityId entityId, Class<?> componentType) {
        // Get the current archetype of the entity
        EntityRecord entityRecord = entityMap.get(entityId);
        if (entityRecord == null) {
            throw new RuntimeException("Cannot add component to entity. Entity is not in the register.");
        }
        Archetype archetype = entityRecord.getArchetype();

        // Get the next archetype of the entity
        Archetype nextArchetype = getRemoveArchetype(archetype, componentType);

        // Move the entity into its new archetype. Since this archetype will not have a column for the component, it
        // will automatically get discarded.
        int row = moveEntity(archetype, entityRecord.getRow(), nextArchetype);
        entityRecord.setArchetype(nextArchetype);
        entityRecord.setRow(row);
    }

    private Archetype getAddArchetype(Archetype archetype, Class<?> componentType) {
        // Get the edges of the archetype
        ArchetypeEdge archetypeEdge = archetype.archetypeEdgeMap().getOrDefault(componentType, new ArchetypeEdge());

        // Check if the next archetype is cached
        if (archetypeEdge.getAddArchetype() == null) {
            // ...If not then
            LOGGER.trace("Finding archetype of {} with component {}", archetype, componentType);

            // Create the entity type of the desired next archetype
            EntityType entityType = new EntityType(new HashSet<>(archetype.type().components()));
            entityType.components().add(componentType);

            // Check if the archetype exists
            Archetype addArchetype = archetypeMap.get(entityType);
            if (addArchetype == null) {
                // ...If not then create and register it
                addArchetype = new Archetype(entityType);
                registerArchetype(addArchetype);
            }

            // Cache the archetype
            archetypeEdge.setAddArchetype(addArchetype);
            archetype.archetypeEdgeMap().put(componentType, archetypeEdge);
        }

        // Return the archetype
        return archetypeEdge.getAddArchetype();
    }

    private Archetype getRemoveArchetype(Archetype archetype, Class<?> componentType) {
        // Get the edges of the archetype
        ArchetypeEdge archetypeEdge = archetype.archetypeEdgeMap().getOrDefault(componentType, new ArchetypeEdge());

        // Check if the next archetype is cached
        if (archetypeEdge.getRemoveArchetype() == null) {
            // ...If not then
            LOGGER.trace("Finding archetype of {} without component {}", archetype, componentType);

            // Create the entity type of the desired next archetype
            EntityType entityType = new EntityType(new HashSet<>(archetype.type().components()));
            entityType.components().remove(componentType);

            // Check if the archetype exists
            Archetype removeArchetype = archetypeMap.get(entityType);
            if (removeArchetype == null) {
                // ...If not then create and register it
                removeArchetype = new Archetype(entityType);
                registerArchetype(removeArchetype);
            }

            // Cache the archetype
            archetypeEdge.setRemoveArchetype(removeArchetype);
        }

        // Return the archetype
        return archetypeEdge.getRemoveArchetype();
    }

    private int moveEntity(Archetype oldArchetype, int oldRow, Archetype nextArchetype) {
        LOGGER.trace("Moving row {} of table from {} to {}", oldRow, oldArchetype, nextArchetype);

        // Create a new entry in the table of the next archetype
        Table oldTable = oldArchetype.table();
        Table nextTable = nextArchetype.table();

        int newRow = nextTable.newRow();

        // And for every component in the next archetype
        for (Class<?> componentType : nextArchetype.type().components()) {
            LOGGER.trace("Copying {} from {} to {}", componentType, oldArchetype, nextArchetype);

            // Get the archetype records for the component
            Map<Archetype, ArchetypeRecord> archetypes = componentMap.get(componentType);
            if (archetypes == null) {
                throw new RuntimeException("Failed to move entity row to new archetype. Component in archetype is not registered.");
            }

            // Check if the component exists in the old archetype
            if (!archetypes.containsKey(oldArchetype)) {
                // If not, then just leave the slot empty.
                LOGGER.trace("{} does not have component {}", oldArchetype, componentType);
                continue;
            }

            // Get the component out of the old table
            ArchetypeRecord oldArchetypeRecord = archetypes.get(oldArchetype);
            Object component = oldTable.get(oldArchetypeRecord.column(), oldRow);

            // ...And store it in the next table
            ArchetypeRecord nextArchetypeRecord = archetypes.get(nextArchetype);
            nextTable.set(nextArchetypeRecord.column(), newRow, component);
        }

        // And finally remove the entry from the table of the previous archetype
        oldTable.removeRow(oldRow);

        return newRow;
    }

    private void registerArchetype(Archetype archetype) {
        // Check if the archetype is already registered
        if (archetypeMap.containsKey(archetype.type())) {
            LOGGER.warn("Archetype already in the register");
            return;
        }

        // Register the archetype in the map
        archetypeMap.put(archetype.type(), archetype);

        // Register the archetype in its components
        int column = 0;
        for (Class<?> componentType : archetype.type().components()) {
            // Ensure that the component has been registered
            registerComponent(componentType);

            // Register the archetype in the component map
            Map<Archetype, ArchetypeRecord> archetypes = componentMap.get(componentType);
            archetypes.put(archetype, new ArchetypeRecord(column));

            // Move on to the next component type
            column++;
        }

        LOGGER.trace("Registered archetype {}", archetype);
    }

    private void registerComponent(Class<?> component) {
        // Check if the component is already registered
        if (componentMap.containsKey(component)) {
            return;
        }

        // Register the component
        componentMap.put(component, new HashMap<>());

        LOGGER.trace("Registered component {}", component);
    }

    private void registerEntity(EntityId entityId) {
        // Put the entity in the empty archetype
        entityMap.put(entityId, new EntityRecord(EMPTY_ARCHETYPE, -1));

        LOGGER.trace("Registered entity {}", Integer.toHexString(entityId.id()));
    }

    private void unregisterEntity(EntityId entityId) {
        // Get the archetype of the entity
        EntityRecord entityRecord = entityMap.get(entityId);
        if (entityRecord == null) {
            return;
        }
        Archetype archetype = entityRecord.getArchetype();

        // Remove the entity from the archetype
        archetype.table().removeRow(entityRecord.getRow());
    }
}
