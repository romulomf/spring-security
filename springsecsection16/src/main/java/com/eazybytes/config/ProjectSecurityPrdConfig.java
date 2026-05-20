package com.eazybytes.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpsRedirectConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.eazybytes.exceptionhandling.CustomAccessDeniedHandler;
import com.eazybytes.filter.CsrfCookieFilter;

@Configuration
@Profile("prd")
public class ProjectSecurityPrdConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
		CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
		http.sessionManagement(smc -> smc
				/**
				 * A configuração de session fixation, determina que estratégia usar para proteger do ataque de session
				 * fixation ataque, onde um usuário pode "roubar" o cookie de um outro usuário e com isso ter detalhes
				 * pessoais associados a conta do usuário a qual a conta foi roubada.
				 */
//				.sessionFixation(sfc -> sfc.changeSessionId())
				/**
				 * Como a aplicação irá confiar em tokens JWT para o processo de autenticação/autorização, a aplicação
				 * no servidor não precisa registrar uma sessão no backend. A aplicação passa a ser stateless, isto é,
				 * ela não armazena informações de estado, pois todas as informações que precisam ser validadas, estão
				 * presentes no token que o cliente irá encaminhar para executar ações privilegiadas.
				 */
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//				.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true)
		);
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
		// habilita a proteção CSRF.
		http.csrf(csrfConfig -> csrfConfig
				.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
				.ignoringRequestMatchers("/contact", "/register")
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
			.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
		// a configuração de canais a seguir, exige que seja usado canal seguro, ou seja apenas HTTPS.
		http.redirectToHttps(HttpsRedirectConfigurer::disable);
		http.authorizeHttpRequests(req -> req
//			.requestMatchers("/myAccount").hasAuthority("VIEWACCOUNT")
//			.requestMatchers("/myBalance").hasAnyAuthority("VIEWBALANCE", "VIEWACCOUNT")
//			.requestMatchers("/myCards").hasAuthority("VIEWCARDS")
//			.requestMatchers("/myLoans").hasAuthority("VIEWLOANS")
			.requestMatchers("/myAccount").hasRole("USER")
			.requestMatchers("/myBalance").hasAnyRole("USER", "ADMIN")
			.requestMatchers("/myCards").hasRole("USER")
			.requestMatchers("/myLoans").authenticated()
			.requestMatchers("/user").authenticated()
			.requestMatchers("/contact", "/error", "/notices", "/register").permitAll()
		);
		http.oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		/**
		 * A configuração que indica false para requireExplicitSave indica que a aplicação não irá armazenar
		 * nenhuma informação sobre o cookie JSESSIONID nem detalhes de autenticação no SecurityContextHolder
		 * e indica ao spring security que ele deve cuidar desta tarefa.
		 * 
		 * Como a aplicação passou a ser stateless, esta configuração não é mais necessária, pois como não
		 * está sendo mais gerenciado o estado, não há também mais a geração do cookie JSESSIONID.
		 */
//		http.securityContext(scc -> scc.requireExplicitSave(false));
		/**
		 * A configuração a seguir é a uma outra forma de tratar as exceções de acesso não autorizado HTTP 401.
		 * Esta é uma configuração global que irá prover a resposta com base na configuração customizada que
		 * for atribuída.
		 */
		http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}