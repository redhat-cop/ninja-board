plugins {
    application
}

application {
    mainClassName = "com.redhat.services.ninja.data.Migration"
}

dependencies {
    implementation("jakarta.json:jakarta.json-api:1.1.6")
    implementation("jakarta.json.bind:jakarta.json.bind-api:1.0.2")

    implementation(project(":entities"))

    runtimeOnly("org.glassfish:jakarta.json:1.1.6")
    runtimeOnly("org.eclipse:yasson:1.0.7")
}