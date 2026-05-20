package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.eazybytes.filter.CsrfCookieFilter;

@Configuration
@Profile("prd")
public class ProjectSecurityPrdConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// habilita a proteção CSRF.
		CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
		http.csrf(csrfConfig -> csrfConfig
				.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
				.ignoringRequestMatchers("/contact", "/register")
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
		.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
		http.cors(corsConfig -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
			/**
			 * Permite que a configuração CORS atenda a requisições de quaisquer que sejam os
			 * verbos HTTP utilizados nas requisições, tais como DELETE, GET, POST, PATCH ou PUT.
			 */
			config.setAllowedMethods(Collections.singletonList("*"));
			config.setAllowCredentials(true);
			config.setAllowedHeaders(Collections.singletonList("*"));
			config.setMaxAge(3600L);
			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", config);
			corsConfig.configurationSource(source);
		});
		http.sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true));
		// a configuração de canais a seguir, exige que seja usado canal seguro, ou seja apenas HTTPS que é o padrão, então não precisa especificar nada.
		http.authorizeHttpRequests(req -> req
//			.requestMatchers("/myAccount").hasAuthority("VIEWACCOUNT")
//			.requestMatchers("/myBalance").hasAnyAuthority("VIEWBALANCE", "VIEWACCOUNT")
//			.requestMatchers("/myCards").hasAuthority("VIEWCARDS")
//			.requestMatchers("/myLoans").hasAuthority("VIEWLOANS")
			.requestMatchers("/myAccount").hasRole("USER")
			.requestMatchers("/myBalance").hasAnyRole("USER", "ADMIN")
			.requestMatchers("/myCards").hasRole("USER")
			.requestMatchers("/myLoans").hasRole("USER")
			.requestMatchers("/user").authenticated()
			.requestMatchers("/contact", "/error", "/invalidSession", "/notices", "/register").permitAll()
		);
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