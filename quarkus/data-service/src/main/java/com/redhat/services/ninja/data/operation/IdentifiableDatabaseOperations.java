package com.redhat.services.ninja.data.operation;

import com.redhat.services.ninja.data.operation.BasicDatabaseOperations;

public interface IdentifiableDatabaseOperations<I, T> extends BasicDatabaseOperations<T> {
    T get(I identifier);

    default T update(T entity) {
        return create(entity);
    }

    T delete(I identifier);
}
