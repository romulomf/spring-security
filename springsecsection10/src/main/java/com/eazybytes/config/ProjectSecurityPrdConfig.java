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

import com.eazybytes.filter.AuthoritiesLoggingAfterFilter;
import com.eazybytes.filter.AuthoritiesLoggingAtFilter;
import com.eazybytes.filter.CsrfCookieFilter;
import com.eazybytes.filter.RequestValidationBeforeFilter;

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
		.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
		.addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class)
		.addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class)
		/**
		 * addFilterAt faz com que o filtro seja executado aleatoriamente antes ou depois do
		 * filtro ao qual ele foi executado. não há como controlar isso, e isso pode variar
		 * entre as requisições. é um comportamento um tanto esquisito com uma aplicabilidade
		 * prática muito pouco comum de ser usada.
		 */
		.addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class);
		http.cors(corsConfig -> corsConfig.configurationSource(request -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedOrigins(Collections.singletonList("https://localhost:4200"));
			config.setAllowedMethods(Collections.singletonList("*"));
			config.setAllowCredentials(true);
			config.setAllowedHeaders(Collections.singletonList("*"));
			config.setMaxAge(3600L);
			return config;
		}));
		http.sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true));
		// a configuração de canais a seguir, exige que seja usado canal seguro, ou seja apenas HTTPS.
		http.requiresChannel(rcf -> rcf.anyRequest().requiresSecure());
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