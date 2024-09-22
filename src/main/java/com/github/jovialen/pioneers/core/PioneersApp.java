package com.github.jovialen.pioneers.core;

import com.github.jovialen.engine.ecs.Register;
import com.github.jovialen.engine.ecs.entity.Entity;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class PioneersApp {
    public static void main(String[] args) {
        Register register = new Register();

        List<Entity> entities = new ArrayList<>();
        entities.add(register.createEntity());
        entities.getLast().setName("Max");
        entities.getLast().set(10);
        entities.getLast().set(true);

        entities.add(register.createEntity());
        entities.getLast().setName("Jean");
        entities.getLast().set(9);
        entities.getLast().set(false);

        entities.getLast().destroy();
        entities.removeLast();

        entities.add(register.createEntity());
        entities.getLast().setName("Bob");
        entities.getLast().set(18);
        entities.getLast().set(true);
        entities.getLast().remove(String.class);

        entities.add(register.createEntity());
        entities.getLast().setName("Alice");
        entities.getLast().setName("Laura");
        entities.getLast().set(22);
        entities.getLast().set(false);

        for (Entity entity : entities) {
            Logger.info("Hi, {}!", entity.getName());
            Logger.info("You are {} years old", entity.get(Integer.class));
            Logger.info("You are a {}", (boolean) entity.get(Boolean.class) ? "boy" : "girl");
        }
    }
}
