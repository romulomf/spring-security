package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpsRedirectConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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
import com.eazybytes.filter.AuthoritiesLoggingAfterFilter;
import com.eazybytes.filter.AuthoritiesLoggingAtFilter;
import com.eazybytes.filter.CsrfCookieFilter;
import com.eazybytes.filter.JWTTokenGeneratorFilter;
import com.eazybytes.filter.JWTTokenValidatorFilter;
import com.eazybytes.filter.RequestValidationBeforeFilter;

@Configuration
@Profile("!prd")
public class ProjectSecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
				.ignoringRequestMatchers("/apiLogin", "/contact", "/register")
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
			.addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class)
			.addFilterAfter(new JWTTokenGeneratorFilter(), BasicAuthenticationFilter.class)
			.addFilterBefore(new JWTTokenValidatorFilter(), BasicAuthenticationFilter.class);
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
			.requestMatchers("/apiLogin", "/contact", "/error", "/invalidSession", "/notices", "/register").permitAll()
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

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		EazyBankUsernamePwdAuthenticationProvider authenticationProvider = new EazyBankUsernamePwdAuthenticationProvider(userDetailsService);
		ProviderManager providerManager = new ProviderManager(authenticationProvider);
		/**
		 * Definir setEraseCredentialsAfterAuthentication com false, faz com que a senha não seja excluída do objeto
		 * de autenticação, após ele ter sido concluído. Isto permite que a senha possa ser usada para qualquer que
		 * seja o outro processo que ela seja requerida em uma autenticação customizada.
		 */
		providerManager.setEraseCredentialsAfterAuthentication(false);
		return providerManager;
	}
}