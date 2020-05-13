package com.redhat.services.ninja.data.service;

import java.util.Collection;
import java.util.List;

class BasicDatabaseOperationsImpl<T> implements com.redhat.services.ninja.data.operation.BasicDatabaseOperations<T> {

    private final Collection<T> entities;

    public BasicDatabaseOperationsImpl(Collection<T> entities) {
        this.entities = entities;
    }

    @Override
    public T create(T entity) {
        entities.add(entity);
        return entity;
    }

    @Override
    public List<T> getAll() {
        return List.copyOf(entities);
    }
}

