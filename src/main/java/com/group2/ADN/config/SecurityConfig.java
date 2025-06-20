package com.group2.ADN.config;

import com.group2.ADN.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/paypal/**"
                        ).permitAll()

                        // Customer endpoints
                        .requestMatchers(HttpMethod.POST, "/tickets/**").hasRole("CUSTOMER")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")

                        // Staff endpoints
                        .requestMatchers("/staff/**").hasRole("STAFF")

                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Shared endpoints
                        .requestMatchers(HttpMethod.PUT, "/tickets/*/assign").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tickets/*/status").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
