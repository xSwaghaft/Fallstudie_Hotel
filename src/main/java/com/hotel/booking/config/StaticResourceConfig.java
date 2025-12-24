package com.hotel.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve images directly from the project folder so newly uploaded files are available immediately
        // (without needing to rebuild/copy resources to target/classes).
        Path imageRoot = Paths.get("src", "main", "resources", "static", "images").toAbsolutePath().normalize();
        String location = imageRoot.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }
}
