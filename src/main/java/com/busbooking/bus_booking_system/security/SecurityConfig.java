package com.busbooking.bus_booking_system.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final Environment environment;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            Environment environment,
            UserDetailsService userDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.environment = environment;
        this.userDetailsService = userDetailsService;
    }

    /* =========================================================
       AUTHENTICATION PROVIDER (CRITICAL FIX)
    ========================================================= */

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /* =========================================================
       SECURITY FILTER CHAIN
    ========================================================= */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        boolean isProd = Arrays.asList(environment.getActiveProfiles())
                .contains("prod");

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            /* 🔥 THIS LINE FIXES LOGIN */
            .authenticationProvider(authenticationProvider())

            .headers(headers -> {

                headers.frameOptions(frame -> frame.deny());

                headers.referrerPolicy(referrer ->
                        referrer.policy(
                                ReferrerPolicyHeaderWriter
                                        .ReferrerPolicy
                                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                        )
                );

                if (isProd) {
                    headers.httpStrictTransportSecurity(hsts ->
                            hsts.includeSubDomains(true)
                                    .maxAgeInSeconds(31536000)
                    );
                }

                headers.contentSecurityPolicy(csp ->
                        csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' https://checkout.razorpay.com; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data: https://tile.openstreetmap.org https:; " +
                                "connect-src 'self' https: wss: ws:; " +
                                "font-src 'self' https://fonts.gstatic.com; " +
                                "frame-src https://checkout.razorpay.com;"
                        )
                );
            })

            .authorizeHttpRequests(auth -> auth

                    .requestMatchers("/", "/index.html").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()

                    .requestMatchers(
                            "/api/trails/**",
                            "/api/themes/**",
                            "/api/yatra-points/**",
                            "/api/circuits/**",
                            "/api/buses/**"
                    ).permitAll()

                    .requestMatchers("/ws/**").permitAll()

                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    .requestMatchers(
                            "/api/bookings/**",
                            "/api/seats/**",
                            "/api/users/**",
                            "/api/payments/**",
                            "/api/tickets/**"
                    ).authenticated()

                    .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class)

            .addFilterAfter(rateLimitFilter,
                    JwtAuthenticationFilter.class)

            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(
                            (request, response, authException) -> {
                                String path = request.getRequestURI();
                                if (path.startsWith("/api/admin")) {
                                    response.sendError(
                                            HttpServletResponse.SC_FORBIDDEN,
                                            "Forbidden"
                                    );
                                } else {
                                    response.sendError(
                                            HttpServletResponse.SC_UNAUTHORIZED,
                                            "Unauthorized"
                                    );
                                }
                            }
                    )
                    .accessDeniedHandler(
                            (request, response, accessDeniedException) ->
                                    response.sendError(
                                            HttpServletResponse.SC_FORBIDDEN,
                                            "Forbidden"
                                    )
                    )
            );

        return http.build();
    }

    /* =========================================================
       CORS
    ========================================================= */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        String frontendUrl = environment.getProperty(
                "app.frontend.url",
                "http://localhost:3000"
        );

        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /* =========================================================
       AUTH MANAGER
    ========================================================= */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /* =========================================================
       PASSWORD ENCODER
    ========================================================= */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
