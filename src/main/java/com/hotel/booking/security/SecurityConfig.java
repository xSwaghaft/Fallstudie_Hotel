package com.hotel.booking.security;

import com.hotel.booking.view.LoginView;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for Vaadin and Spring Security integration.
 * <p>
 * This class enables view protection in Vaadin Flow using Spring Security annotations
 * such as {@code @AnonymousAllowed}, {@code @PermitAll}, and {@code @RolesAllowed}.
 * It configures security filter chains for public resources and authenticated requests,
 * and integrates Vaadin's security context with Spring Security.
 * </p>
 * 
 * @author Artur Derr
 * @see com.hotel.booking.view.LoginView
 * @see VaadinSecurityConfigurer
 */
@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain publicResourcesFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/images/**", "/VAADIN/**", "/frontend/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                configurer.loginView(LoginView.class));

        return http.build();
    }
}
