package com.redhat.services.ninja.data.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.entity.User;
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
class UserResourceTest extends AbstractResourceTest {

    @Test
    void create() {
        User newUser = new User();
        newUser.setUsername("new_ninja");
        newUser.setDisplayName("New Ninja");
        newUser.setGithubUsername("new_ninja");
        newUser.setRegion("EMEA");

        User createdUser = given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .put("/data/user")
                .as(User.class);

        assertEquals(newUser, createdUser);
    }

    @Test
    void get() {
        Map<String, User> users = getUsers();

        User superNinja = given()
                .when()
                .get("/data/user/super_ninja")
                .as(User.class);

        assertEquals(users.get("super_ninja"), superNinja);
    }

    @Test
    void getAll() {
        Map<String, User> users = getUsers();

        List<User> allUsers = given()
                .when()
                .get("/data/user")
                .as(new TypeRef<>() {
                });

        assertAll(
                () -> assertTrue(allUsers.contains(users.get("modest_ninja"))),
                () -> assertTrue(allUsers.contains(users.get("super_ninja")))
        );
    }

    @Test
    void update() {
        Map<String, User> users = getUsers();

        User modestNinja = users.get("modest_ninja");
        modestNinja.setDisplayName("Modest Ninja");

        given()
                .contentType(ContentType.JSON)
                .body(modestNinja)
                .when()
                .put("/data/user")
                .then()
                .body("displayName", equalTo("Modest Ninja"));
    }

    @Test
    void delete() {
        Map<String, User> users = getUsers();

        User retiredNinja = users.get("retired_ninja");

        User user = given()
                .when()
                .delete("/data/user/retired_ninja")
                .as(User.class);

        assertEquals(retiredNinja, user);
    }

    public Map<String, User> getUsers() {
        return ninjaDatabase.getUsers().stream().collect(Collectors.toMap(User::getUsername, Function.identity()));
    }
}