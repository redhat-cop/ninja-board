package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.entity.User;
import com.redhat.services.ninja.data.service.DatabaseEngine;
import com.redhat.services.ninja.data.operation.IdentifiableDatabaseOperations;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/data/user")
public class UserResource extends AbstractIdentifiableResource<String, User, IdentifiableDatabaseOperations<String,User>> {

    @Inject
    DatabaseEngine engine;

    @Override
    public DatabaseEngine getEngine() {
        return engine;
    }

    @Override
    public IdentifiableDatabaseOperations<String,User> getOperations() {
        return engine.getUserOperations();
    }
}