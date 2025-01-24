package net.furizon.backend.infrastructure.security.configuration;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequiredManager;
import net.furizon.backend.infrastructure.security.filter.DatabaseSessionFilter;
import net.furizon.backend.infrastructure.security.filter.InternalBasicFilter;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final DatabaseSessionFilter databaseSessionFilter;

    private final InternalBasicFilter internalBasicFilter;

    private final SecurityConfig securityConfig;

    @Bean
    public SecurityFilterChain internalFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/internal/**")
            .cors(AbstractHttpConfigurer::disable)
            .csrf(CsrfConfigurer::disable)
            .addFilterAt(
                internalBasicFilter,
                BasicAuthenticationFilter.class
            )
            .build();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor permissionsAdvisor(PermissionFinder permissionFinder) {
        return AuthorizationManagerBeforeMethodInterceptor.preAuthorize(
            new PermissionRequiredManager(permissionFinder)
        );
    }

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
                    antMatcher(HttpMethod.GET, "/docs/**"),
                    antMatcher(HttpMethod.GET, "/swagger-ui/**"),
                    antMatcher(HttpMethod.GET, "/api/v1/events/**"),
                    antMatcher(HttpMethod.GET, "/api/v1/counts/bopos"),
                    antMatcher(HttpMethod.GET, "/api/v1/counts/fursuit"),
                    antMatcher(HttpMethod.GET, "/api/v1/counts/sponsors"),
                    antMatcher(HttpMethod.POST, "/api/v1/authentication/login"),
                    antMatcher(HttpMethod.POST, "/api/v1/authentication/register"),
                    antMatcher(HttpMethod.GET, "/api/v1/authentication/confirm-mail"),
                    antMatcher(HttpMethod.GET, "/api/v1/authentication/pw/status"),
                    antMatcher(HttpMethod.POST, "/api/v1/authentication/pw/reset"),
                    antMatcher(HttpMethod.POST, "/api/v1/authentication/pw/change"),
                    antMatcher(HttpMethod.GET, "/api/v1/states/get-countries"),
                    antMatcher(HttpMethod.GET, "/api/v1/states/by-country"),
                    antMatcher(HttpMethod.GET, "/api/v1/admin/countdown"),
                    antMatcher(HttpMethod.GET, "/api/v1/admin/ping")
                )
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

    private CorsConfigurationSource corsConfigurationSource() {
        final var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(securityConfig.getAllowedOrigins());
        corsConfiguration.setAllowedMethods(
            List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.OPTIONS.name()
            )
        );
        corsConfiguration.setAllowedHeaders(
            List.of("*")
        );
        corsConfiguration.setAllowCredentials(true);

        final var urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return urlBasedCorsConfigurationSource;
    }
}
