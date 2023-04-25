import org.gradle.api.artifacts.dsl.RepositoryHandler;

import java.net.URI;

public class InnoMaven {
    /**
     * Adds the Innovattic nexus repository.
     * @param local If true, also adds the `mavenLocal` repository
     */
    public static void innovattic(RepositoryHandler handler, boolean local) {
        handler.maven((it) -> {
            it.setUrl(URI.create("https://maven.innovattic.com/content/repositories/com.innovattic.backend.releases"));
            it.credentials((creds) -> {
                creds.setUsername("inno-ro");
                creds.setPassword("SpecHu5t");
            });
        });

        if (local) {
            handler.mavenLocal();
        }
    }
}
