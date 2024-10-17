package net.furizon.backend.service.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.db.repositories.users.AuthenticationDataRepository;
import net.furizon.backend.db.repositories.users.UserRepository;
import net.furizon.backend.utils.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Deprecated
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AuthenticationDataRepository authenticationDataRepository;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public static final int MAX_USER_SECRET_GENERATION_TRIES = 5;

    // TODO -> Better use Database unique index instead of this implementation
    // In worst case we will call database 5 times
    // And this code looks pretty wierd c:
    @NotNull
    public String createUniqueSecret() {
        String toReturn = null;
        for (int t = 0; t < MAX_USER_SECRET_GENERATION_TRIES; t++) {
            toReturn = UUID.randomUUID().toString();
            if (userRepository.findBySecret(toReturn).isEmpty()) {
                break;
            }
            toReturn = null;
        }
        if (TextUtil.isEmpty(toReturn)) {
            throw new DuplicateKeyException(
                "Failed to generate secret after " + MAX_USER_SECRET_GENERATION_TRIES + " times."
            );
        }
        return toReturn;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
