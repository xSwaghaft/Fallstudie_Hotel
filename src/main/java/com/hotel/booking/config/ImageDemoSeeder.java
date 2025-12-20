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
 * Seeds demo images from classpath resources into a writable filesystem folder exactly once.
 *
 * <p><b>Background:</b> Resources inside a packaged (Docker) JAR are not writable. However, the
 * application must be able to delete images at runtime (e.g. via ImageManagement) without them
 * being restored automatically on the next restart.
 *
 * <p><b>Approach:</b>
 * <ul>
 *   <li>On startup, copies all files from {@code classpath:/static/images/rooms/*} to
 *       {@code ${app.images.root-dir}/rooms} (only if the target file does not already exist).</li>
 *   <li>Afterwards, writes a marker file to prevent re-seeding on subsequent restarts.</li>
 * </ul>
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>{@code app.images.seed.enabled} (default: {@code true}) – enables/disables seeding</li>
 *   <li>{@code app.images.root-dir} (default: {@code data/images}) – root directory for image files</li>
 *   <li>{@code app.images.seed.marker-file} (default: {@code .seeded-demo-images}) – marker filename</li>
 * </ul>
 *
 * <p><b>Implementation note:</b> The marker file ensures that deleting demo images at runtime remains
 * effective and they are not automatically restored on restart.
 *
 * @author Artur Derr
 */
@Component
public class ImageDemoSeeder implements ApplicationRunner {

    private final boolean enabled;
    private final Path imagesRootDir;
    private final String markerFileName;

    /**
     * Creates a new seeder using values from Spring configuration.
     *
     * @param enabled whether seeding should run ({@code app.images.seed.enabled})
     * @param imagesRootDir root directory as string ({@code app.images.root-dir}); will be resolved
     *                     to a normalized, absolute {@link Path}
     * @param markerFileName marker filename ({@code app.images.seed.marker-file})
     */
    public ImageDemoSeeder(
            @Value("${app.images.seed.enabled:true}") boolean enabled,
            @Value("${app.images.root-dir:data/images}") String imagesRootDir,
            @Value("${app.images.seed.marker-file:.seeded-demo-images}") String markerFileName
    ) {
        this.enabled = enabled;
        this.imagesRootDir = Path.of(imagesRootDir).toAbsolutePath().normalize();
        this.markerFileName = markerFileName;
    }

    /**
     * Performs the one-time seeding at application startup.
     *
     * <p>Early exit conditions:
     * <ul>
     *   <li>Seeding is disabled.</li>
     *   <li>The marker file already exists.</li>
     * </ul>
     *
     * @param args application startup arguments (not evaluated)
     * @throws Exception if copying resources or writing the marker file fails
     */
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

        // Create marker to prevent re-seeding on subsequent restarts. Which ensures that deleting stays effective.
        String markerContent = "seededAt=" + Instant.now() + System.lineSeparator()
                + "copied=" + copied + System.lineSeparator();
        Files.writeString(markerFile, markerContent, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
