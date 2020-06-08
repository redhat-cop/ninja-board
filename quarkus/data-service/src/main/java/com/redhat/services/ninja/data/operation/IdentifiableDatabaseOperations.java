package com.redhat.services.ninja.data.operation;

import com.redhat.services.ninja.entity.Identifiable;

import java.util.Optional;

public interface IdentifiableDatabaseOperations<I, T extends Identifiable<I>> extends BasicDatabaseOperations<T> {
    Optional<T> get(I identifier);

    default T update(T entity) {
        return create(entity);
    }

    T delete(I identifier);
}
