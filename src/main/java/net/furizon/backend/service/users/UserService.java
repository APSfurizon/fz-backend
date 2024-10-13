package net.furizon.backend.service.users;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.db.entities.users.AuthenticationData;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.db.repositories.users.AuthenticationDataRepository;
import net.furizon.backend.db.repositories.users.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.springframework.security.core.userdetails.User.withUsername;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    private final AuthenticationDataRepository authenticationDataRepository;

    private final PasswordEncoder passwordEncoder;

    @NotNull
    public ResponseEntity<String> addUser(User user) {
        ResponseEntity<String> result; // TODO -> Why String? Why not bool?
        try {
            // Password hashing
            String pass = this.passwordEncoder.encode(user.getAuthentication().getPasswordHash());
            user.getAuthentication().setPasswordHash(pass);
            AuthenticationData savedAuth = this.authenticationDataRepository.save(user.getAuthentication());
        } catch (Throwable e) {
            // TODO -> Validate?
        }

        return ResponseEntity.ofNullable(null); //TODO return actual stuff
    }

    @NotNull
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Optional<User> user = this.userRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("Unknown user %s", username));
        }

        return withUsername(user.get().getAuthentication().getEmail())
            .password(user.get().getAuthentication().getPasswordHash())
            .authorities("ROLE_USER")
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(user.get().getAuthentication().isLoginDisabled())
            .build();
    }
}
