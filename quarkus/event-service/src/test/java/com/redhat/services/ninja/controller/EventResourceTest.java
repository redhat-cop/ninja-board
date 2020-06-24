package com.redhat.services.ninja.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.client.EventClient;
import com.redhat.services.ninja.entity.Event;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.common.mapper.TypeRef;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class EventResourceTest extends AbstractResourceTest {
    @InjectMock
    @RestClient
    EventClient client;

    @Test
    void getAllEvents() {
        when(client.getAll())
                .thenAnswer(a -> ninjaDatabase.getEvents());

        List<Event> events = given()
                .when()
                .get("event")
                .as(new TypeRef<>() {
                });

        assertTrue(events.size() > 0);
    }
}