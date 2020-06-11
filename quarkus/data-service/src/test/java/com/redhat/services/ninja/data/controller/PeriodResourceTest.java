package com.redhat.services.ninja.data.controller;

import com.data.services.ninja.test.AbstractResourceTest;
import com.redhat.services.ninja.entity.Level;
import com.redhat.services.ninja.entity.Period;
import com.redhat.services.ninja.entity.Scorecard;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PeriodResourceTest extends AbstractResourceTest {

    @Test
    void create() {
        Period period = new Period();
        period.setName("FY20");

        SortedSet<Level> levels = ninjaDatabase.getLevels();

        ninjaDatabase.getScorecards().forEach(s -> s.computeLevel(levels));

        period.record(ninjaDatabase.getScorecards().toArray(new Scorecard[]{}));

        Period createdPeriod = given()
                .contentType(ContentType.JSON)
                .body(period)
                .when()
                .post("/data/period")
                .as(Period.class);

        assertAll(
                () -> assertEquals(period, createdPeriod)
        );
    }

    @Test
    void getAll() {
        Map<String, Period> periods = getPeriods();

        List<Period> allScorecards = given()
                .when()
                .get("/data/period")
                .as(new TypeRef<>() {
                });

        assertAll(
                () -> assertTrue(allScorecards.contains(periods.get("FY19")))
        );
    }

    @Test
    void update() {
        Map<String, Period> periods = getPeriods();

        Period period = periods.get("FY18");

        period.setName("FY17");

        given()
                .contentType(ContentType.JSON)
                .body(period)
                .when()
                .put("/data/period")
                .then()
                .body("name", is("FY17"));
    }

    @Test
    void delete() {
        Map<String, Period> periods = getPeriods();

        Period period = periods.get("FY16");

        Period scorecard = given()
                .when()
                .delete("/data/period/FY16")
                .as(Period.class);

        assertEquals(period, scorecard);
    }

    private Map<String, Period> getPeriods() {
        return ninjaDatabase.getHistory().stream().collect(Collectors.toMap(Period::getIdentifier, Function.identity()));
    }
}