java {
    sourceSets.main.get().java.srcDirs(
        project(":user-service").projectDir.toPath().resolve("src/main/java"),
        project(":data-service").projectDir.toPath().resolve("src/main/java")
    )

    sourceSets.main.get().resources.srcDirs(
        project(":user-service").projectDir.toPath().resolve("src/main/resources"),
        project(":data-service").projectDir.toPath().resolve("src/main/resources")
    )

    sourceSets.test.get().java.srcDirs(
        project(":user-service").projectDir.toPath().resolve("src/test/java"),
        project(":data-service").projectDir.toPath().resolve("src/test/java")
    )

    sourceSets.test.get().resources.srcDirs(
        project(":user-service").projectDir.toPath().resolve("src/test/resources"),
        project(":data-service").projectDir.toPath().resolve("src/test/resources")
    )
}