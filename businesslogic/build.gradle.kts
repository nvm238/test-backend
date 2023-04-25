dependencies {
    implementation(project(":dbschema"))
    implementation("com.innovattic.backend:innolib-api-client:${DependencyVersions.innolib}")
    api(project(":database"))

    api("org.springframework.boot:spring-boot-starter-websocket")
    api("org.springframework.boot:spring-boot-starter-security")
    api("com.innovattic.backend:innolib-file:${DependencyVersions.innolib}")
    api("com.innovattic.backend:innolib-notification:${DependencyVersions.innolib}")
    api("commons-codec:commons-codec")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("io.mockk:mockk:${DependencyVersions.mockK}")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
