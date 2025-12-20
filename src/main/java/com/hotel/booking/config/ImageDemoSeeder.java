package com.hotel.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Copies demo images from classpath resources into a writable filesystem folder on first startup.
 *
 * Rationale:
 * - Classpath resources inside a packaged JAR are not writable.
 * - ImageManagement must be able to delete files at runtime (especially in Docker).
 *
 * Strategy:
 * - On startup, copy classpath:/static/images/rooms/* into ${app.images.root-dir}/rooms
 * - Create a marker file so deleted demo images won't be re-seeded on restart.
 */
@Component
public class ImageDemoSeeder implements ApplicationRunner {

    private final boolean enabled;
    private final Path imagesRootDir;
    private final String markerFileName;

    public ImageDemoSeeder(
            @Value("${app.images.seed.enabled:true}") boolean enabled,
            @Value("${app.images.root-dir:data/images}") String imagesRootDir,
            @Value("${app.images.seed.marker-file:.seeded-demo-images}") String markerFileName
    ) {
        this.enabled = enabled;
        this.imagesRootDir = Path.of(imagesRootDir).toAbsolutePath().normalize();
        this.markerFileName = markerFileName;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            return;
        }

        Path markerFile = imagesRootDir.resolve(markerFileName);
        if (Files.exists(markerFile)) {
            return;
        }

        Path roomsDir = imagesRootDir.resolve("rooms");
        Files.createDirectories(roomsDir);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/static/images/rooms/*");

        int copied = 0;
        for (Resource resource : resources) {
            if (!resource.exists() || !resource.isReadable()) {
                continue;
            }
            String fileName = resource.getFilename();
            if (fileName == null || fileName.isBlank()) {
                continue;
            }

            Path target = roomsDir.resolve(fileName);
            if (Files.exists(target)) {
                continue;
            }

            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, target);
                copied++;
            }
        }

        // Create marker to prevent re-seeding on subsequent restarts.
        // This ensures that deleting demo images via ImageManagement stays effective.
        String markerContent = "seededAt=" + Instant.now() + System.lineSeparator()
                + "copied=" + copied + System.lineSeparator();
        Files.writeString(markerFile, markerContent, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
