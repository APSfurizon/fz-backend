package net.furizon.backend.infrastructure.security.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.filter.DatabaseSessionFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

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
            // Show unauthorized error
//            .exceptionHandling(customizer -> {
//                customizer.authenticationEntryPoint(
//                    (request, response, authException) -> {
//                        response.sendError(401, "Unauthorized");
//                    });
//            })
            .addFilterAt(
                databaseSessionFilter,
                BasicAuthenticationFilter.class
            )
            .build();
        // if we will implement token based auth - we don't need it (remove it if will not plan to use csrf)
        //http.csrf(csrf -> {
        //csrf
        //.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        //.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler());
        //}).addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
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

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        //daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            /*
             * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
             * the CsrfToken when it is rendered in the response body.
             */
            this.delegate.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            /*
             * If the request contains a request header, use CsrfTokenRequestAttributeHandler
             * to resolve the CsrfToken. This applies when a single-page application includes
             * the header value automatically, which was obtained via a cookie containing the
             * raw CsrfToken.
             */
            if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
                return super.resolveCsrfTokenValue(request, csrfToken);
            }
            /*
             * In all other cases (e.g. if the request contains a request parameter), use
             * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
             * when a server-side rendered form includes the _csrf request parameter as a
             * hidden input.
             */
            return this.delegate.resolveCsrfTokenValue(request, csrfToken);
        }
    }

    final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            FilterChain filterChain
        ) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            // Render the token value to a cookie by causing the deferred token to be loaded
            csrfToken.getToken();
            filterChain.doFilter(request, response);
        }
    }
}
