package net.furizon.backend.service.users;

import jakarta.transaction.Transactional;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.db.entities.users.AuthenticationData;
import net.furizon.backend.db.repositories.users.IAuthenticationDataRepository;
import net.furizon.backend.db.repositories.users.IUserRepository;
import net.furizon.backend.security.entities.UserSecurity;
import net.furizon.backend.security.filters.OneTimePasswordFilter;
import net.furizon.backend.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
	private final IUserRepository userRepository;
	private final IAuthenticationDataRepository authenticationDataRepository;
	private final PasswordEncoder passwordEncoder;

	private Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
	public UserService(IUserRepository userRepository, IAuthenticationDataRepository authenticationDataRepository, PasswordEncoder passwordEncoder) {
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

	@Transactional
	public User register (String username, String password) {
		Optional<User> usr = userRepository.findByEmail(username);
		if (usr.isPresent()) throw new IllegalArgumentException("The user already exists");
		User toRegister = new User(this.createUniqueSecret());
		toRegister = userRepository.save(toRegister);

		AuthenticationData auth = new AuthenticationData();
		auth.setEmail(username);
		auth.setPasswordHash(passwordEncoder.encode(password));
		auth.setAuthenticationOwner(toRegister);
		auth = authenticationDataRepository.save(auth);
		toRegister = userRepository.findById(toRegister.getId()).orElse(null);
		return toRegister;
	}

	public static final int MAX_USER_SECRET_GENERATION_TRIES = 5;

	public String createUniqueSecret(){
		String toReturn = null;
		for (int t = 0; t < MAX_USER_SECRET_GENERATION_TRIES; t++) {
			toReturn = UUID.randomUUID().toString();
			if (userRepository.findBySecret(toReturn).isEmpty()){
				break;
			}
			toReturn = null;
		}
		if (TextUtil.isEmpty(toReturn)) {
			throw new DuplicateKeyException("Failed to generate secret after " + MAX_USER_SECRET_GENERATION_TRIES + " times.");
		}
		return toReturn;
	}
}
