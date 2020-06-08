package com.redhat.services.ninja.entity;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

public class ErrorResponse {
    private String message;

    @JsonbCreator
    public ErrorResponse(@JsonbProperty("message") String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
