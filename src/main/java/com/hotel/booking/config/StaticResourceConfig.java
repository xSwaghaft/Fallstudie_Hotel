package com.hotel.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configures static resource handling for images served from a writable filesystem directory.
 *
 * <p><b>Purpose:</b> Classpath resources inside a JAR are read-only and cannot be modified at runtime.
 * This configuration enables serving images from a configurable external directory, which can be a
 * mounted volume in Docker or a local filesystem path during development.
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>{@code app.images.root-dir} (default: {@code data/images}) – root directory where images are stored</li>
 * </ul>
 *
 * <p>All HTTP requests to {@code /images/**} are mapped to the configured filesystem directory.
 *
 * @author Artur Derr
 * @see ImageDemoSeeder
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String imagesRootDir;

    /**
     * Creates a new static resource configuration with the images root directory.
     *
     * @param imagesRootDir the root directory path where images are stored; will be resolved to an
     *                      absolute, normalized path
     */
    public StaticResourceConfig(@Value("${app.images.root-dir:data/images}") String imagesRootDir) {
        this.imagesRootDir = imagesRootDir;
    }

    /**
     * Registers resource handlers to serve images from the configured filesystem directory.
     *
     * <p>Maps all requests matching {@code /images/**} to the absolute filesystem path configured
     * via {@code app.images.root-dir}, ensuring trailing slash in the location URL.
     *
     * @param registry the {@link ResourceHandlerRegistry} to register handlers with
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve images from a writable filesystem folder (configurable). This works both in local dev
        // and in Docker (when mounted as a volume), without relying on classpath resources.
        Path imageRoot = Paths.get(imagesRootDir).toAbsolutePath().normalize();
        String location = imageRoot.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/images/**")
                // First try the writable filesystem directory (uploads/runtime images)
                .addResourceLocations(location)
                // Fallback to bundled classpath images (e.g., login background)
                .addResourceLocations("classpath:/static/images/");
    }
}
