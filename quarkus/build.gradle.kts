plugins {
    id("io.quarkus") version "1.4.2.Final" apply false
}

subprojects {
    group = "com.redhat.service.ninja"
    version = "2.0.0-SNAPSHOT"

    if (this.name.endsWith("service")) {
        apply(plugin = "java")
        apply(plugin = "io.quarkus")

        dependencies {
            "implementation"(enforcedPlatform("io.quarkus:quarkus-universe-bom:1.4.2.Final"))
            "implementation"("io.quarkus:quarkus-resteasy-jsonb")
            "implementation"("io.quarkus:quarkus-resteasy")
            "implementation"(project(":entities"))

            "testImplementation"("io.quarkus:quarkus-junit5")
            "testImplementation"("io.rest-assured:rest-assured")
        }

        tasks {
            withType<Test> {
                exclude("**/Native*")
            }
        }
    } else {
        apply(plugin = "java")
    }

    repositories {
        mavenCentral()
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }
}