import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin

buildscript {
    repositories {
        mavenCentral()
        val mavenLocal: String by project
        InnoMaven.innovattic(this, mavenLocal.equals("true", true))
    }
    dependencies {
        classpath("com.innovattic.backend:innolib-gradle-plugins:${DependencyVersions.innolib}")
    }
}

plugins {
    base
    kotlin("jvm") version DependencyVersions.kotlin
    id("org.springframework.boot") version DependencyVersions.springBoot apply false
    id("io.gitlab.arturbosch.detekt") version DependencyVersions.detekt
    id("com.github.ben-manes.versions") version DependencyVersions.versionsPlugin
}

allprojects {
    repositories {
        mavenCentral()
        val mavenLocal: String by project
        InnoMaven.innovattic(this, mavenLocal.equals("true", true))
    }
}

subprojects {
    group = "com.innovattic.medicinfo"
    version = "0.1.0-SNAPSHOT"

    // Override Spring-managed versions
    extra["kotlin.version"] = DependencyVersions.kotlin
    extra["rest-assured.version"] = DependencyVersions.restAssured

    apply(plugin = "org.jetbrains.kotlin.jvm")
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType(KotlinCompile::class).all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    apply(plugin = "io.spring.dependency-management")
    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom(SpringBootPlugin.BOM_COORDINATES)
        }
    }

    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        buildUponDefaultConfig = true
        // If we want to enable integrationtest here, we'll need to tell Detekt that integrationtest is a test source set
        source = files("src/main/kotlin", "src/generator/kotlin", "src/test/kotlin"/*, "src/integrationtest/kotlin"*/)
        config = files(rootProject.file("detekt-config.yml"))
        autoCorrect = System.getenv("DETEKT_AUTOCORRECT") != null
    }
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
        // xml reports for ci
        reports {
            xml.required.set(true)
        }
    }

    apply(plugin = "com.innovattic.gradle.integrationtest")

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${DependencyVersions.detekt}")

        api(kotlin("stdlib-jdk8"))
        api("org.jetbrains.kotlin:kotlin-reflect")

        api("org.slf4j:slf4j-api")

        testImplementation(project(":testutil"))
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

        "integrationTestImplementation"(project(":testutil"))

    }
}

tasks.withType<DependencyUpdatesTask> {
    checkForGradleUpdate = true
}

// add task to generate an elasticbeans bundle
project(":web").afterEvaluate {
    val bootJarTask = tasks.getByName("bootJar")
    rootProject.tasks.register<Zip>("eb") {
        archiveFileName.set("medicinfo-eb.zip")
        destinationDirectory.set(file("${rootProject.buildDir}/dist"))
        
        from(".platform") {
            into(".platform")
        }
        from(bootJarTask.outputs.files)
        
        dependsOn(bootJarTask)
    }
}
