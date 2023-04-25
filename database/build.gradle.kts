dependencies {
    api(project(":dbschema"))
    api(project(":dbschema", "generated"))

    implementation("com.innovattic.backend:innolib-jooq:${DependencyVersions.innolib}")
    api("org.springdoc:springdoc-openapi-ui:${DependencyVersions.springDoc}")

    runtimeOnly("org.postgresql:postgresql")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
