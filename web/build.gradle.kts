import org.springframework.boot.gradle.tasks.bundling.BootJar

logger.warn("Running on Java version ${System.getProperty("java.version")}")

plugins {
    id("org.springframework.boot")
    id("com.gorylenko.gradle-git-properties") version DependencyVersions.gradleGitPropertiesPlugin
}

dependencies {
    api(project(":businesslogic"))

    implementation("software.amazon.awssdk:sns:${DependencyVersions.aws}")
    api("com.innovattic.backend:innolib-web:${DependencyVersions.innolib}")
    implementation("software.amazon.awssdk:sns:${DependencyVersions.aws}")
    implementation("com.innovattic.backend:innolib-api-client:${DependencyVersions.innolib}")
    implementation("org.springdoc:springdoc-openapi-security:${DependencyVersions.springDoc}")
    implementation("org.springdoc:springdoc-openapi-kotlin:${DependencyVersions.springDoc}")

    // NOTE: io.awspring depends on micrometer-registery-cloudwatch v1.x (https://github.com/awspring/spring-cloud-aws/issues/20)
    implementation("io.awspring.cloud:spring-cloud-aws-autoconfigure:${DependencyVersions.springCloud}")
    // spring-boot-actuator-autoconfigure is needed for spring-cloud-aws-autoconfigure to work
    implementation("org.springframework.boot:spring-boot-actuator-autoconfigure:${DependencyVersions.springBoot}")
    implementation("io.micrometer:micrometer-registry-cloudwatch:${DependencyVersions.micrometer}")

    integrationTestImplementation("org.hildan.krossbow:krossbow-stomp-core:4.1.0")
    integrationTestImplementation("org.hildan.krossbow:krossbow-websocket-builtin:4.1.0")
    integrationTestImplementation("io.rest-assured:rest-assured")
    // Spring dependency management has a version for rest-assured, but not for its kotlin-extensions
    integrationTestImplementation("io.rest-assured:kotlin-extensions:${DependencyVersions.restAssured}")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// by default, the spring boot plugin disables jar (in favor of bootJar),
// but we need it, as our jar is used as a dependency for other modules (like integrationtest)
tasks.getByName<Jar>("jar") {
    enabled = true
}

tasks.getByName<BootJar>("bootJar") {
    archiveClassifier.set("boot")
}

// Configuration for bootJar and bootRun tasks 
springBoot {
    mainClass.set("com.innovattic.medicinfo.ApplicationKt")
}
