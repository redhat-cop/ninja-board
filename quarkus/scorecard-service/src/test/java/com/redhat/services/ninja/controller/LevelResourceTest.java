package com.redhat.services.ninja.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.client.LevelClient;
import com.redhat.services.ninja.entity.Level;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.common.mapper.TypeRef;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
class LevelResourceTest extends AbstractResourceTest {
    @InjectMock
    @RestClient
    LevelClient client;

    @Test
    void getAll() {
        when(client.getAll())
                .thenReturn(new ArrayList<>(super.ninjaDatabase.getLevels()));

        List<Level> levels = given()
                .when()
                .get("level")
                .as(new TypeRef<>() {
                });

        assertAll(
                () -> assertEquals(5, levels.size()),
                () -> assertEquals("ZERO", levels.get(0).getName()));
    }

}