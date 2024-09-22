package com.github.jovialen.engine.ecs;

import com.github.jovialen.engine.ecs.entity.Entity;
import com.github.jovialen.engine.ecs.entity.EntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RegisterTest {
    Register register;

    @BeforeEach
    void setUp() {
        this.register = new Register();
    }

    @Test
    void createDestroy() {
        EntityId entityId1 = register.create();
        EntityId entityId2 = register.create();
        EntityId entityId3 = register.create();

        Assertions.assertEquals(entityId1.id(), 0);
        Assertions.assertEquals(entityId1.generation(), 1);

        Assertions.assertEquals(entityId2.id(), 1);
        Assertions.assertEquals(entityId2.generation(), 1);

        Assertions.assertEquals(entityId3.id(), 2);
        Assertions.assertEquals(entityId3.generation(), 1);

        register.destroy(entityId1);
        entityId1 = register.create();

        Assertions.assertEquals(entityId1.id(), 0);
        Assertions.assertEquals(entityId1.generation(), 2);

        EntityId entityId4 = register.create();

        Assertions.assertEquals(entityId4.id(), 3);
        Assertions.assertEquals(entityId4.generation(), 1);
    }

    @Test
    void createDestroyEntity() {
        Entity entity1 = register.createEntity();
        Entity entity2 = register.createEntity();

        Assertions.assertEquals(entity1.entityId().id(), 0);
        Assertions.assertEquals(entity1.register(), register);
        Assertions.assertEquals(entity1.entityId().generation(), 1);

        Assertions.assertEquals(entity2.entityId().id(), 1);
        Assertions.assertEquals(entity2.register(), register);
        Assertions.assertEquals(entity2.entityId().generation(), 1);
    }

    @Test
    void checkAlive() {
        EntityId entity = register.create();
        Assertions.assertTrue(register.checkAlive(entity));

        register.destroy(entity);
        Assertions.assertFalse(register.checkAlive(entity));

        EntityId entity2 = register.create();
        EntityId entity3 = register.create();

        Assertions.assertFalse(register.checkAlive(entity));
        Assertions.assertTrue(register.checkAlive(entity2));
        Assertions.assertTrue(register.checkAlive(entity3));

        register.destroy(entity3);
        Assertions.assertFalse(register.checkAlive(entity3));
    }

    @Test
    void hasComponent() {
        EntityId entity1 = register.create();
        register.addComponent(entity1, "Entity1");
        register.addComponent(entity1, 10);
        register.addComponent(entity1, false);

        EntityId entity2 = register.create();
        register.addComponent(entity2, "Entity2");
        register.addComponent(entity2, 20);

        Assertions.assertTrue(register.hasComponent(entity1, String.class));
        Assertions.assertTrue(register.hasComponent(entity1, Integer.class));
        Assertions.assertTrue(register.hasComponent(entity1, Boolean.class));

        Assertions.assertTrue(register.hasComponent(entity2, String.class));
        Assertions.assertTrue(register.hasComponent(entity2, Integer.class));
        Assertions.assertFalse(register.hasComponent(entity2, Boolean.class));

        register.removeComponent(entity1, Integer.class);
        register.addComponent(entity2, false);

        Assertions.assertFalse(register.hasComponent(entity1, Integer.class));
        Assertions.assertTrue(register.hasComponent(entity2, Boolean.class));
    }

    @Test
    void getComponent() {
        EntityId entity1 = register.create();
        EntityId entity2 = register.create();

        register.addComponent(entity1, 15);
        register.addComponent(entity2, 30);

        Assertions.assertEquals(register.getComponent(entity1, Integer.class), 15);
        Assertions.assertEquals(register.getComponent(entity2, Integer.class), 30);
        Assertions.assertNull(register.getComponent(entity1, String.class));

        register.removeComponent(entity1, Integer.class);
        register.addComponent(entity2, 20);

        Assertions.assertNull(register.getComponent(entity1, Integer.class));
        Assertions.assertEquals(register.getComponent(entity2, Integer.class), 20);
    }

    @Test
    void addComponent() {
        for (int i = 0; i < 5; i++) {
            EntityId entityId = register.create();

            register.addComponent(entityId, 10);
            Assertions.assertTrue(register.hasComponent(entityId, Integer.class));
            Assertions.assertEquals(register.getComponent(entityId, Integer.class), 10);

            register.addComponent(entityId, 15);
            Assertions.assertTrue(register.hasComponent(entityId, Integer.class));
            Assertions.assertEquals(register.getComponent(entityId, Integer.class), 15);

            register.addComponent(entityId, true);
            Assertions.assertTrue(register.hasComponent(entityId, Integer.class));
            Assertions.assertEquals(register.getComponent(entityId, Integer.class), 15);
            Assertions.assertTrue(register.hasComponent(entityId, Boolean.class));
            Assertions.assertTrue((Boolean) register.getComponent(entityId, Boolean.class));
        }
    }

    @Test
    void removeComponent() {
        EntityId entityId = register.create();

        register.removeComponent(entityId, String.class);
        register.removeComponent(entityId, Integer.class);
        register.removeComponent(entityId, Boolean.class);

        register.addComponent(entityId, "");
        register.addComponent(entityId, 1);
        Assertions.assertNotEquals(register.getComponent(entityId, String.class), null);
        Assertions.assertNotEquals(register.getComponent(entityId, Integer.class), null);

        register.removeComponent(entityId, String.class);
        Assertions.assertNull(register.getComponent(entityId, String.class));
        Assertions.assertNotEquals(register.getComponent(entityId, Integer.class), null);

        register.removeComponent(entityId, Integer.class);
        Assertions.assertNull(register.getComponent(entityId, String.class));
        Assertions.assertNull(register.getComponent(entityId, Integer.class));
    }

    @Test
    void manyEntities() {
        List<EntityId> entities = new ArrayList<>();

        for (int i = 0; i < 100_000; i++) {
            EntityId entityId = register.create();
            register.addComponent(entityId, i);
            register.addComponent(entityId, i % 2 == 0);
            entities.add(entityId);
        }

        for (int i = 0; i < entities.size(); i++) {
            Assertions.assertEquals(register.getComponent(entities.get(i), Boolean.class), i % 2 == 0);
            Assertions.assertEquals(register.getComponent(entities.get(i), Integer.class), i);

            if (i % 3 == 0) {
                register.destroy(entities.get(i));
            }
        }

        for (int i = 0; i < entities.size(); i++) {
            Assertions.assertEquals(register.checkAlive(entities.get(i)), i % 3 != 0);
        }
    }
}