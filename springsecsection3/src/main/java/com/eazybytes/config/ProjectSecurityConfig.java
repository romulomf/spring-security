package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

@Configuration
public class ProjectSecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(req -> req
			.requestMatchers("/myAccount", "/myBalance", "/myCards", "/myLoans").authenticated()
			.requestMatchers("/contact", "/error", "/notices").permitAll()
		);
		http.formLogin(withDefaults());
		http.httpBasic(withDefaults());
		return http.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		// cria novos usuários
		// user {noop} na string com a senha, indica que não deve ser usado nenhum password enconder com a senha
		UserDetails user = User
				.withUsername("user")
				.password("{noop}EazyBytes@123456")
				.authorities("read")
				.build();
		UserDetails admin = User
				.withUsername("admin")
				.password("{bcrypt}$2a$12$PYZy13spiKJXfaz0vzaw8.wrnbZlkd9uaIDLpQ0.4b9QrUTvTA1R.")
				.authorities("admin")
				.build();
		// cria um gerenciador de usuários em memória
		return new InMemoryUserDetailsManager(user, admin);
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/**
	 * {@link CompromisedPasswordChecker} permite criar um serviço de verifica
	 * se uma senha foi comprometida (vazada) publicamente e usado de maneira
	 * maliciosa.
	 * 
	 * Caso o usuário tenha uma senha que foi comprometida, o serviço faz essa
	 * verificação e retorna um alerta para a aplicação.
	 */
	@Bean
	CompromisedPasswordChecker compromisedPasswordChecker() {
		return new HaveIBeenPwnedRestApiPasswordChecker();
	}
}