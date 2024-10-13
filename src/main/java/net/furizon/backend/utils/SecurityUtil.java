package net.furizon.backend.utils;

import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.web.entities.exceptions.ApiException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

@Slf4j
public class SecurityUtil {
    private static final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    /**
     * Get the authenticated user from the SecurityContextHolder
     * @throws net.furizon.backend.web.entities.exceptions.ApiException if the user is not found in the SecurityContextHolder
     */
    public static User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }else {
            log.error("User requested but not found in SecurityContextHolder");
            throw ApiException.builder().status(401).message("Authentication required").build();
        }
    }

    public static Optional<User> getOptionalAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }
}
