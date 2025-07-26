    package com.group2.ADN.config;

    import com.group2.ADN.security.JwtAuthenticationFilter;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.security.config.Customizer;

    @Configuration
    @RequiredArgsConstructor
    public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/auth/**",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**",
                                    "/webjars/**",
                                    "/api/paypal/**", // Cho phép public
                                    "/api/prices/**", // Allow public access to price endpoint
                                    "/api/blogs/**"   // Allow public access to blog endpoint
                            ).permitAll()

                            // Admin Access
                            .requestMatchers("/admin/**").hasRole("ADMIN")

                            // Role-based access - Allow CUSTOMER, STAFF, ADMIN for ticket operations
                            .requestMatchers("/customer/**").hasRole("CUSTOMER")
                            .requestMatchers("/staff/**").hasRole("STAFF")
                            .requestMatchers("/notifications/**").hasRole("CUSTOMER")
                            .requestMatchers("/admin/reviews").hasRole("ADMIN")
                            .requestMatchers("/admin/tickets-with-feedback").hasRole("ADMIN")

                            .requestMatchers(HttpMethod.PUT, "/tickets/*/assign").hasAnyRole("STAFF", "ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/tickets/*/status").hasAnyRole("STAFF", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/tickets/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/tickets/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                            .requestMatchers(HttpMethod.POST, "/notifications/**").hasAnyRole("STAFF", "ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/notifications/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/notifications/**").hasAnyRole("STAFF", "ADMIN")

                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .formLogin(form -> form.disable())
                    .httpBasic(basic -> basic.disable())
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint((request, response, authException) -> {
                                response.setStatus(401);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Access denied - Invalid or missing token\"}");
                            })
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                response.setStatus(403);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Insufficient permissions - You don't have access to this resource\"}");
                            })
                    )
                    .build();
        }
    }
