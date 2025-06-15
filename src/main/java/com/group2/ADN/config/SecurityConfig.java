    package com.group2.ADN.config;

    import com.group2.ADN.security.JwtAuthenticationFilter;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                                    "/api/paypal/**" // Cho phép public
                            ).permitAll()

                            // Role-based access
                            .requestMatchers("/customer/**").hasRole("CUSTOMER")
                            .requestMatchers("/staff/**").hasRole("STAFF")
                            .requestMatchers("/admin/**").hasRole("ADMIN")

                            .requestMatchers(HttpMethod.PUT, "/api/tickets/*/assign").hasAnyRole("STAFF", "ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/tickets/*/status").hasAnyRole("STAFF", "ADMIN")

                            .requestMatchers(HttpMethod.GET, "/api/tickets/**").hasRole("CUSTOMER")
                            .requestMatchers(HttpMethod.POST, "/api/tickets/**").hasRole("CUSTOMER")

                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .formLogin(form -> form.disable())
                    .httpBasic(basic -> basic.disable())
                    .build();
        }
    }
