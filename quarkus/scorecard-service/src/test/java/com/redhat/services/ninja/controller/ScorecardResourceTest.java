package com.redhat.services.ninja.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.client.ScorecardClient;
import com.redhat.services.ninja.entity.Scorecard;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class ScorecardResourceTest extends AbstractResourceTest {
    @InjectMock
    @RestClient
    ScorecardClient client;

    static private final Map<String, Scorecard> scorecards = new HashMap<>();


    @BeforeAll
    static void initScorecards() {
        Scorecard newScorecard = new Scorecard();
        newScorecard.setUsername("new_ninja");

        scorecards.put("new_ninja", newScorecard);
    }

    @BeforeEach
    void initTest() {
        when(client.create(any(Scorecard.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(client.update(any(Scorecard.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(client.get(any())).thenAnswer(a -> scorecards.get(a.getArgument(0)));

        when(client.getAll()).thenReturn(new ArrayList<>(scorecards.values()));
    }

    @Test
    void increment() {
        Scorecard scorecard = given()
                .contentType(ContentType.TEXT)
                .body(2)
                .when()
                .post("scorecard/new_ninja/Trello")
                .as(Scorecard.class);

        assertAll(
                () -> assertEquals("new_ninja", scorecard.getUsername()),
                () -> assertEquals(2, scorecard.getTotal()),
                () -> assertEquals(2, scorecard.getDetails().get("Trello"))
        );
    }

    @Test
    void incrementNonExistingScorecard() {
        given()
                .when()
                .body(3)
                .post("scorecard/non_existing_ninja/Trello")
                .then()
                .statusCode(404);
    }

    @Test
    void getExistingScorecard() {
        Scorecard scorecard = given()
                .when()
                .get("scorecard/new_ninja")
                .as(Scorecard.class);

        assertEquals("new_ninja", scorecard.getUsername());
    }

    @Test
    void getNonExistingScorecard() {
        given()
                .when()
                .get("scorecard/non_existing_ninja")
                .then()
                .statusCode(404);
    }

    @Test
    void getAllScorecards() {
        List<Scorecard> scorecards = given()
                .when()
                .get("scorecard")
                .as(new TypeRef<>() {
                });

        for (int i = 0; i < scorecards.size() - 1; i++) {
            assertTrue(scorecards.get(i).getTotal() >= scorecards.get(i + 1).getTotal());
        }
    }
}