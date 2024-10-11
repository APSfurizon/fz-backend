package net.furizon.backend.service.users;

import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.db.repositories.users.IAuthenticationDataRepository;
import net.furizon.backend.db.repositories.users.IUserRepository;
import net.furizon.backend.security.entities.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

@Service
public class UserSecurityService implements UserDetailsService {

    private final IUserRepository userRepository;
    private final IAuthenticationDataRepository authenticationDataRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserSecurityService(IUserRepository userRepository, IAuthenticationDataRepository authenticationDataRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationDataRepository = authenticationDataRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> usr = userRepository.findByEmail(username);
        if (usr.isEmpty()) throw new IllegalArgumentException("User not found");
        return new UserSecurity(usr.get());
    }
}
