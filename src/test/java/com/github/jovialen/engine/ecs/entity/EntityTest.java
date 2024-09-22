package com.github.jovialen.engine.ecs.entity;

import com.github.jovialen.engine.ecs.Register;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntityTest {
    Register register;

    @BeforeEach
    void setUp() {
        register = new Register();
    }

    @Test
    void createDestroy() {
        Entity entity = register.createEntity();

        Assertions.assertEquals(entity.entityId().id(), 0);
        Assertions.assertEquals(entity.entityId().generation(), 1);
        Assertions.assertTrue(entity.isAlive());
        Assertions.assertEquals(entity.register(), register);

        entity.destroy();
        Assertions.assertFalse(entity.isAlive());
    }
}