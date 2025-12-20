package com.hotel.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String imagesRootDir;

    public StaticResourceConfig(@Value("${app.images.root-dir:data/images}") String imagesRootDir) {
        this.imagesRootDir = imagesRootDir;
    }

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
                .addResourceLocations(location);
    }
}
