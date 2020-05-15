package com.redhat.services.ninja.data.service;

import com.redhat.services.ninja.entity.Identifiable;

import java.util.List;
import java.util.Map;

class IdentifiableDatabaseOperationsImpl<I, T extends Identifiable<I>> implements com.redhat.services.ninja.data.operation.IdentifiableDatabaseOperations<I,T> {
    private final Map<I, T> entities;

    public IdentifiableDatabaseOperationsImpl(Map<I, T> entities) {
        this.entities = entities;
    }

    @Override
    public T create(T entity) {
        entities.put(entity.getIdentifier(), entity);
        return entity;
    }

    @Override
    public List<T> getAll() {
        return List.copyOf(entities.values());
    }

    @Override
    public T get(I identifier) {
        return entities.get(identifier);
    }

    @Override
    public T delete(I identifier) {
        return entities.remove(identifier);
    }
}

