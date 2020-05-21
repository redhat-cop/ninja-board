package com.data.services.ninja.test;

import com.redhat.services.ninja.entity.Database;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

@QuarkusTest
public abstract class AbstractResourceTest {

    @ConfigProperty(name = "database.file", defaultValue = "database.json")
    String databaseLocation;
    
    protected Database ninjaDatabase;
    
    @BeforeAll
    static void init() {
        RestAssured.defaultParser = Parser.JSON;
    }

    @BeforeEach
    void initEach() throws IOException {
        Jsonb jsonb = JsonbBuilder.create();
        ninjaDatabase = jsonb.fromJson(getDatabaseStream(), Database.class);
        Path path = Paths.get(databaseLocation);
        Files.write(path, getDatabaseStream().readAllBytes());
        Logger.getAnonymousLogger().log(Level.INFO, path.toAbsolutePath().toString());
    }
    
    protected InputStream getDatabaseStream(){
        return AbstractResourceTest.class.getClassLoader().getResourceAsStream("/com/redhat/services/ninja/test/database.json");
    }
}
