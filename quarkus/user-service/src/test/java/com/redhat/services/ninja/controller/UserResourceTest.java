package com.redhat.services.ninja.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.client.EventClient;
import com.redhat.services.ninja.client.ScorecardClient;
import com.redhat.services.ninja.client.UserClient;
import com.redhat.services.ninja.entity.Event;
import com.redhat.services.ninja.entity.Scorecard;
import com.redhat.services.ninja.entity.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserResourceTest extends AbstractResourceTest {

    @InjectMock
    @RestClient
    UserClient userClient;

    @InjectMock
    @RestClient
    ScorecardClient scorecardClient;

    @InjectMock
    @RestClient
    EventClient eventClient;

    @BeforeEach
    void initTest() {
        when(userClient.create(any(User.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(scorecardClient.create(any(Scorecard.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(eventClient.create(any(Event.class)))
                .thenAnswer(a -> a.getArgument(0));
    }

    @Test
    void existingUserRegistration() {
        User newUser = new User();
        newUser.setUsername("new_ninja");
        newUser.setDisplayName("New Ninja");
        newUser.setGithubUsername("new_ninja");

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("user")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body(
                        "username", equalTo("new_ninja"),
                        "displayName", equalTo("New Ninja"),
                        "githubUsername", equalTo("new_ninja"),
                        "region", equalTo("EMEA")
                );
    }

    @Test
    void nonExistingUserRegistration() {
        User newUser = new User();
        newUser.setUsername("no_ninja");
        newUser.setDisplayName("No Ninja");
        newUser.setGithubUsername("no_ninja");

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("user")
                .then()
                .statusCode(404);
    }

    @Test
    void noUserContentRegistration() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("user")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}