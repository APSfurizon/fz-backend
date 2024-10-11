package net.furizon.backend.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.db.repositories.users.IUserRepository;
import net.furizon.backend.service.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class OneTimePasswordFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private IUserRepository userRepository;

    private Logger log = LoggerFactory.getLogger(OneTimePasswordFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            User usr = userRepository.findByEmail(auth.getName()).orElse(null);
            if (usr.getAuthentication().is2faEnabled()) {

            }
        }
        // Continue to filter
        filterChain.doFilter(request, response);
    }
}
