import io.quarkus.gradle.tasks.QuarkusBuild

plugins {
    id("io.quarkus") version "1.4.2.Final" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.quarkus")

    group = "com.redhat.service.ninja"
    version = "2.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"(enforcedPlatform("io.quarkus:quarkus-universe-bom:1.4.2.Final"))
        "implementation"("io.quarkus:quarkus-resteasy-jsonb")
        "implementation"("io.quarkus:quarkus-resteasy")

        "testImplementation"("io.quarkus:quarkus-junit5")
        "testImplementation"("io.rest-assured:rest-assured")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
            exclude("**/Native*")
        }
    }
}