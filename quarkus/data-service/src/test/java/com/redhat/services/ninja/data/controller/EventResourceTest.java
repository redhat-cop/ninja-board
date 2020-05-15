package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.entity.Event;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class EventResourceTest extends AbstractResourceTest {

    @Test
    void create() {
        Event event = new Event();
        event.setUser("new_user");
        event.setType("New User Registered");

        Event createdEvent = given()
                .contentType(ContentType.JSON)
                .body(event)
                .when()
                .post("/data/event")
                .as(Event.class);

        assertAll(
                () -> assertEquals(event.getUser(), createdEvent.getUser()),
                () -> assertEquals(event.getType(), createdEvent.getType()),
                () -> assertNotNull(event.getTimestamp())
        );
    }

    @Test
    void getAll() {
        List<Event> allEvents = given()
                .when()
                .get("/data/event")
                .as(new TypeRef<>() {
                });

        assertAll(
                () -> assertTrue(allEvents.stream().anyMatch(e -> e.getUser().equals("super_ninja"))),
                () -> assertTrue(allEvents.stream().anyMatch(e -> e.getUser().equals("modest_ninja")))
        );
    }
}