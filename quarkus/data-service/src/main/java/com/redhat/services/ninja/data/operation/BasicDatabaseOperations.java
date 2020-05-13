package com.redhat.services.ninja.data.operation;

import java.util.List;

public interface BasicDatabaseOperations<T> {

    T create(T entity);

    List<T> getAll();
}
