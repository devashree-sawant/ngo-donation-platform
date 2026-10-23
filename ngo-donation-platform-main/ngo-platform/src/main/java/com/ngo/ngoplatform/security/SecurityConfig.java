package com.ngo.ngoplatform.security;

import com.ngo.ngoplatform.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(
                        (request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ngos", "/api/donors").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/needs", "/api/needs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ngos", "/api/ngos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/needs").hasRole("NGO")
                        .requestMatchers(HttpMethod.PUT, "/api/needs/**").hasRole("NGO")
                        .requestMatchers(HttpMethod.DELETE, "/api/needs/**").hasRole("NGO")
                        .requestMatchers(HttpMethod.POST, "/api/contributions").hasRole("DONOR")
                        .requestMatchers(HttpMethod.GET, "/api/donors/*/contributions").hasRole("DONOR")
                        .requestMatchers(HttpMethod.GET, "/api/contributions").hasRole("NGO")
                        .requestMatchers(HttpMethod.GET, "/api/donors").hasRole("NGO")
                        .requestMatchers(HttpMethod.GET, "/api/donors/*").hasAnyRole("DONOR", "NGO")
                        .requestMatchers("/api/donors/**").hasRole("DONOR")
                        .requestMatchers("/api/ngos/**").hasRole("NGO")
                        .requestMatchers("/api/contributions/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // The filter belongs in Spring Security's chain only; disable servlet-container auto-registration.
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterServletRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
