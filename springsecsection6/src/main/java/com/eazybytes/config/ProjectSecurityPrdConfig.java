package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

@Configuration
@Profile("prd")
public class ProjectSecurityPrdConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(req -> req
			.requestMatchers("/myAccount", "/myBalance", "/myCards", "/myLoans").authenticated()
			.requestMatchers("/contact", "/error", "/notices", "/register").permitAll()
		);
		/* por padrão, o Spring Security bloqueia chamadas REST que modificam o estado o que quer
		 * dizer que as requisições que modificam dados cujos verbos HTTP são PUT, PATCH, POST e
		 * DELETE estão por padrão com a proteção CSRF habilitada e se não for feita a configuração
		 * ou o desligamento desta proteção, chamar endpoints com estes verbos HTTP resultará em
		 * falha na autorização HTTP 403.
		 */
		http.csrf(CsrfConfigurer::disable);
		http.formLogin(withDefaults());
		http.httpBasic(withDefaults());
		return http.build();
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