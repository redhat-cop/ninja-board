package com.redhat.services.ninja.data.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.entity.Level;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LevelResourceTest extends AbstractResourceTest {

    @Test
    void create() {
        Level newLevel = new Level();
        newLevel.setMinimumPoint(150);
        newLevel.setName("GOLD");

        Level createdLevel = given()
                .contentType(ContentType.JSON)
                .body(newLevel)
                .when()
                .put("/data/level")
                .as(Level.class);

        assertEquals(newLevel, createdLevel);
    }

    @Test
    void get() {
        Map<String, Level> levels = getLevels();

        Level blackLevel = given()
                .when()
                .get("/data/level/BLACK")
                .as(Level.class);

        assertEquals(levels.get("BLACK"), blackLevel);
    }

    @Test
    void getAll() {
        Map<String, Level> levels = getLevels();

        List<Level> allLevels = given()
                .when()
                .get("/data/level")
                .as(new TypeRef<>() {
                });

        assertAll(
                () -> assertTrue(allLevels.contains(levels.get("ZERO"))),
                () -> assertTrue(allLevels.contains(levels.get("RED"))),
                () -> assertTrue(allLevels.contains(levels.get("GREY"))),
                () -> assertTrue(allLevels.contains(levels.get("BLACK")))
        );
    }

    @Test
    void update() {
        Map<String, Level> levels = getLevels();

        Level level = levels.get("BLACK");
        level.setMinimumPoint(90);

        given()
                .contentType(ContentType.JSON)
                .body(level)
                .when()
                .put("/data/level")
                .then()
                .body("minimumPoint", is(90));
    }

    @Test
    void delete() {
        Map<String, Level> levels = getLevels();

        Level blue = levels.get("BLUE");

        Level level = given()
                .when()
                .delete("/data/level/BLUE")
                .as(Level.class);

        given().when()
                .get("/data/level/BLUE")
                .then()
                .statusCode(404);

        assertEquals(blue, level);
    }

    public Map<String, Level> getLevels() {
        return ninjaDatabase.getLevels().stream().collect(Collectors.toMap(Level::getIdentifier, Function.identity()));
    }
}