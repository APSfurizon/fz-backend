package net.furizon.backend.infrastructure.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// sessionId -> randomUUID (index = hash one)
// sessionId -> token (public/private info)

// Authentication Filter Chain -> Per Request -> Do we have Auth token in header

@Component
public class DatabaseSessionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        // TODO -> get and validate request.getHeader(HttpHeaders.AUTHORIZATION)
        // if null just do next filter chain

        // validate token
        // get sessionId and find session in database

        // setup SecurityContextHolder or send fail error

        filterChain.doFilter(request, response);
    }
}
