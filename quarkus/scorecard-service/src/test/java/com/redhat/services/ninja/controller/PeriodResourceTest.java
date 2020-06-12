package com.redhat.services.ninja.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.client.PeriodClient;
import com.redhat.services.ninja.client.ScorecardClient;
import com.redhat.services.ninja.entity.Period;
import com.redhat.services.ninja.entity.Scorecard;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class PeriodResourceTest extends AbstractResourceTest {
    @InjectMock
    @RestClient
    ScorecardClient scorecardClient;

    @InjectMock
    @RestClient
    PeriodClient periodClient;

    @BeforeEach
    void initTest() {
        when(scorecardClient.create(any(Scorecard.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(scorecardClient.update(any(Scorecard.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(scorecardClient.getAll()).thenAnswer(a -> new ArrayList<>(ninjaDatabase.getScorecards()));

        when(periodClient.create(any(Period.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(periodClient.update(any(Period.class)))
                .thenAnswer(a -> a.getArgument(0));

        when(periodClient.get(any())).thenAnswer(a ->
                ninjaDatabase.getHistory().stream()
                        .filter(p -> p.getName().equals(a.getArgument(0))).findAny().orElse(null)
        );
    }

    @Test
    void create() {
        Period period = given()
                .contentType(ContentType.TEXT)
                .when()
                .post("history/FY20")
                .as(Period.class);

        assertAll(
                () -> assertEquals("FY20", period.getIdentifier()),
                () -> assertNotEquals(0, period.getRecords().size())
        );
    }

    @Test
    void getExistingPeriod() {
        Period period = given()
                .when()
                .get("history/FY19")
                .as(Period.class);

        assertEquals("FY19", period.getIdentifier());
    }

    @Test
    void getNonExistingScorecard() {
        given()
                .when()
                .get("history/FY10")
                .then()
                .statusCode(404);
    }
}