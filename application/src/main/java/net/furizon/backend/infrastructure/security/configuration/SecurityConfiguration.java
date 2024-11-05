package net.furizon.backend.infrastructure.security.configuration;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.filter.DatabaseSessionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final DatabaseSessionFilter databaseSessionFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Map the allowed endpoints
        return http
            .cors(customizer ->
                customizer.configurationSource(corsConfigurationSource())
            )
            .csrf(CsrfConfigurer::disable)
            .authorizeHttpRequests(customizer -> customizer
                .requestMatchers(
                    antMatcher(HttpMethod.POST, "/api/v1/authentication/login"),
                    antMatcher(HttpMethod.POST, "/api/v1/authentication/register")
                )
                .permitAll()
                // TODO -> Remove it later (just for testing)
                .requestMatchers("/internal/**")
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .addFilterAt(
                databaseSessionFilter,
                BasicAuthenticationFilter.class
            )
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(
                List.of("*") // TODO -> Replace for prod
            );
            config.setAllowedMethods(
                List.of(
                    HttpMethod.GET.name(),
                    HttpMethod.POST.name(),
                    HttpMethod.PUT.name(),
                    HttpMethod.DELETE.name(),
                    HttpMethod.PATCH.name(),
                    HttpMethod.OPTIONS.name()
                )
            );
            config.setAllowedHeaders(
                List.of("*")
            );
            config.setAllowCredentials(true);
            return config;
        };
    }
}
