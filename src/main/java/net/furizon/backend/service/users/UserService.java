package net.furizon.backend.service.users;

import jakarta.transaction.Transactional;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.db.entities.users.AuthenticationData;
import net.furizon.backend.db.repositories.users.IAuthenticationDataRepository;
import net.furizon.backend.db.repositories.users.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
	private final IUserRepository userRepository;
	private final IAuthenticationDataRepository authenticationDataRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserService(IUserRepository userRepository, IAuthenticationDataRepository authenticationDataRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.authenticationDataRepository = authenticationDataRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public ResponseEntity<String> addUser(User user) {
		ResponseEntity<String> result;
		try {
			// Password hashing
			String pass = this.passwordEncoder.encode(user.getAuthentication().getPasswordHash());
			user.getAuthentication().setPasswordHash(pass);
			AuthenticationData savedAuth = this.authenticationDataRepository.save(user.getAuthentication());
		} catch (Throwable e) {

		}

		return null;//TODO return actual stuff
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final Optional<User> user = this.userRepository.findByEmail(username);
		if (user.isEmpty()){
			throw new UsernameNotFoundException("Unknown user " + username);
		}
		return org.springframework.security.core.userdetails.User.withUsername(user.get().getAuthentication().getEmail())
				.password(user.get().getAuthentication().getPasswordHash())
				.authorities("ROLE_USER")
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(user.get().getAuthentication().isLoginDisabled())
				.build();
	}
}
