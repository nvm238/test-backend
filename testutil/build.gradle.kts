dependencies {
    implementation(project(":dbschema"))
    implementation(project(":database"))
    implementation(project(":web"))

    api("org.mockito.kotlin:mockito-kotlin:3.2.0")
    api("org.mockito:mockito-inline:2.13.0")
    api("io.mockk:mockk:${DependencyVersions.mockK}")
    api("com.innovattic.backend:innolib-test:${DependencyVersions.innolib}")
}
