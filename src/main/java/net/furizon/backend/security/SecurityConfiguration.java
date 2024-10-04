package net.furizon.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

		requestCache.setMatchingRequestParameterName(null);

		// Allow static access
		http.authorizeHttpRequests(configurer -> configurer
				.requestMatchers(HttpMethod.GET, "/css/**", "/js/**", "/img/**", "/images/**", "/client/**")
				.permitAll());

		// Permetti l'accesso a specifici endpoint senza autenticazione
		http.authorizeHttpRequests(configurer -> configurer
				.requestMatchers(HttpMethod.POST, "/auth").permitAll());

		// Configura la gestione delle eccezioni e il form di login
		http.exceptionHandling(configurer -> configurer.accessDeniedPage("/auth/access-denied"))
				.formLogin(form -> form.loginProcessingUrl("/auth").loginPage("/auth/login")
						.defaultSuccessUrl("/furpanel/home", true).permitAll())
				.logout(logout -> logout.logoutUrl("/auth/logout").logoutSuccessUrl("/auth/login")
						.deleteCookies("JSESSIONID").invalidateHttpSession(true).clearAuthentication(true).permitAll());

		// Configurazioni aggiuntive
		http.requestCache((cache) -> cache.requestCache(requestCache));

//        http.formLogin(Customizer.withDefaults());
//        http.logout(Customizer.withDefaults());

		http.cors(cors -> cors.disable());

		http.csrf(csrf -> csrf.disable());
		http.httpBasic(Customizer.withDefaults());

		// Le configurazione con .authenticated() vanno sempre alla fine
		http.authorizeHttpRequests(configurer -> configurer.anyRequest().authenticated());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
