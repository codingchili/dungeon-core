package com.codingchili.realm.instance.model;

import java.util.Optional;

/**
 *
 */
public class SpellTarget {
    private Vector vector;
    private Entity entity;

    public Optional<Vector> getVector() {
        return Optional.ofNullable(vector);
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public Optional<Entity> getEntity() {
        return Optional.ofNullable(entity);
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
