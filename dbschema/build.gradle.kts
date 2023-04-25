import com.innovattic.gradle.JooqGeneratorConfiguration

apply(plugin = "com.innovattic.gradle.jooq-generator")
the<JooqGeneratorConfiguration>().apply {
    mainClass = "com.innovattic.medicinfo.database.generator.GenerateJooqKt"
    dbHostname = System.getenv("POSTGRES_HOST") ?: "localhost"
    dbName = System.getenv("POSTGRES_DATABASE") ?: "medicinfo"
    dbUser = System.getenv("POSTGRES_USER") ?: "postgres"
    dbPassword = System.getenv("POSTGRES_PASSWORD") ?: "admin"
}
