package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpsRedirectConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.eazybytes.exceptionhandling.CustomAccessDeniedHandler;
import com.eazybytes.exceptionhandling.CustomBasicAuthenticationEntryPoint;
import com.eazybytes.filter.CsrfCookieFilter;

@Configuration
@Profile("!prd")
public class ProjectSecurityConfig {

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
		http.sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(3).maxSessionsPreventsLogin(true));
		// a configuração de canais a seguir, permite que seja usado canal inseguro, ou seja HTTP
		http.redirectToHttps(HttpsRedirectConfigurer::disable);
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
		/**
		 * É possível criar um ponto de entrada de autenticação customizado para usar com a autenticação
		 * do tipo HTTP Basic. Isto é uma forma de personalizar o tratamento do fluxo de exceção quando
		 * é tentado o acesso a um recurso protegido, sem ter um usuário autenticado.
		 * 
		 * Esta configuração personalizada de captura de exceções entra em cena apenas no cenário de um
		 * acesso por parte de um usuário não autenticado.
		 */
		http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
		/**
		 * A configuração que indica false para requireExplicitSave indica que a aplicação não irá armazenar
		 * nenhuma informação sobre o cookie JSESSIONID nem detalhes de autenticação no SecurityContextHolder
		 * e indica ao spring security que ele deve cuidar desta tarefa.
		 */
		http.securityContext(scc -> scc.requireExplicitSave(false));
		/**
		 * A configuração a seguir é a uma outra forma de tratar as exceções de acesso não autorizado HTTP 401.
		 * Esta é uma configuração global que irá prover a resposta com base na configuração customizada que
		 * for atribuída.
		 */
		http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));
		http.sessionManagement(smc -> smc
			/**
			 * A configuração de session fixation, determina que estratégia usar para proteger do ataque de session
			 * fixation ataque, onde um usuário pode "roubar" o cookie de um outro usuário e com isso ter detalhes
			 * pessoais associados a conta do usuário a qual a conta foi roubada.
			 */
			.sessionFixation(sfc -> sfc.changeSessionId())
			.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
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