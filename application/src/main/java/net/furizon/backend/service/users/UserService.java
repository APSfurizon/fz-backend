package net.furizon.backend.service.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.db.repositories.users.AuthenticationDataRepository;
import net.furizon.backend.db.repositories.users.UserRepository;
import net.furizon.backend.security.entities.UserSecurity;
import net.furizon.backend.utils.TextUtil;
import net.furizon.backend.web.entities.users.UserLoginRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    //    private final AuthenticationManager authenticationManager;
    private final AuthenticationDataRepository authenticationDataRepository;
    //    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    // TODO -> Should not be here
    // read/write package tho, will implement it with JOOQ
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserSecurity(
            userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
        );
    }

    /**
     * Sets the cookie for the user if the username and password are correct
     */
    // TODO ->This thing should not be in Service,
    //it suppose to be on authentication filter chain
    // by login method we should create session/token and operate with it
    public void login(
        HttpServletRequest request,
        HttpServletResponse response,
        UserLoginRequest body
    ) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
            body.getEmail(),
            body.getPassword()
        );
        //Authentication authentication = authenticationManager.authenticate(token);
        //SecurityContextHolderStrategy securityContextHolderStrategy =
        //SecurityContextHolder.getContextHolderStrategy();
        //SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        //context.setAuthentication(authentication);
        //securityContextHolderStrategy.setContext(context);
        //securityContextRepository.saveContext(context, request, response);
    }

    // it it works?
    //@Transactional
    //public UserResponse getSession(HttpServletRequest request) {
    //User user = SecurityUtil.getAuthenticatedUser();
    //return new UserResponse(user);
    // }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.logoutHandler.logout(request, response, authentication);
    }

    @Transactional
    public User register(String username, String password) {
        //Optional<User> usr = userRepository.findByEmail(username);
        //if (usr.isPresent()) {
        //throw new IllegalArgumentException("The user already exists");
        //}
        //User toRegister = new User(this.createUniqueSecret());
        //toRegister = userRepository.save(toRegister);

        //AuthenticationData auth = new AuthenticationData();
        //auth.setEmail(username);
        //auth.setPasswordHash(passwordEncoder.encode(password));
        //auth.setAuthenticationOwner(toRegister);
        //auth = authenticationDataRepository.save(auth);
        //toRegister = userRepository.findById(toRegister.getId()).orElse(null);
        //return toRegister;
        return null;
    }

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
}
