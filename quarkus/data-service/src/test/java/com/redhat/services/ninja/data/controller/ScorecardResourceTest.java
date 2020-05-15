package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.entity.Scorecard;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ScorecardResourceTest extends AbstractResourceTest{
    @Test
    void create() {
        Scorecard newScorecard = new Scorecard();
        newScorecard.setUsername("new_ninja");
        newScorecard.increment("Github", 2);

        Scorecard createdScorecard = given()
                .contentType(ContentType.JSON)
                .body(newScorecard)
                .when()
                .post("/data/scorecard")
                .as(Scorecard.class);

        assertAll(
                () -> assertEquals(newScorecard, createdScorecard),
                () -> assertEquals(newScorecard.getTotal(), createdScorecard.getTotal())
        );
    }

    @Test
    void getAll() {
        Map<String, Scorecard> scorecards = getScorecards();

        List<Scorecard> allScorecards = given()
                .when()
                .get("/data/scorecard")
                .as(new TypeRef<>() {
                });

        assertAll(
                () -> assertTrue(allScorecards.contains(scorecards.get("modest_ninja"))),
                () -> assertTrue(allScorecards.contains(scorecards.get("super_ninja")))
        );
    }

    @Test
    void update() {
        Map<String, Scorecard> scorecards = getScorecards();

        Scorecard modestNinja = scorecards.get("modest_ninja");
        modestNinja.increment("Trello", 5);

        given()
                .contentType(ContentType.JSON)
                .body(modestNinja)
                .when()
                .put("/data/scorecard")
                .then()
                .body("total", equalTo(7));
    }

    @Test
    void delete() {
        Map<String, Scorecard> scorecards = getScorecards();

        Scorecard retiredNinja = scorecards.get("retired_ninja");

        Scorecard scorecard = given()
                .when()
                .delete("/data/scorecard/retired_ninja")
                .as(Scorecard.class);

        assertEquals(retiredNinja, scorecard);
    }
    
    private Map<String, Scorecard> getScorecards(){
        return ninjaDatabase.getScorecards().stream().collect(Collectors.toMap(Scorecard::getIdentifier, Function.identity()));
    }
}