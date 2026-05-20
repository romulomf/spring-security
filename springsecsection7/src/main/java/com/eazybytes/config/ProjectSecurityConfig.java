package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpsRedirectConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

import com.eazybytes.exceptionhandling.CustomAccessDeniedHandler;
import com.eazybytes.exceptionhandling.CustomBasicAuthenticationEntryPoint;

@Configuration
@Profile("!prd")
public class ProjectSecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(3).maxSessionsPreventsLogin(true));
		// a configuração de canais a seguir, permite que seja usado canal inseguro, ou seja HTTP
		http.redirectToHttps(HttpsRedirectConfigurer::disable);
		http.authorizeHttpRequests(req -> req
			.requestMatchers("/myAccount", "/myBalance", "/myCards", "/myLoans").authenticated()
			.requestMatchers("/contact", "/error", "/invalidSession", "/notices", "/register").permitAll()
		);
		/* por padrão, o Spring Security bloqueia chamadas REST que modificam o estado o que quer
		 * dizer que as requisições que modificam dados cujos verbos HTTP são PUT, PATCH, POST e
		 * DELETE estão por padrão com a proteção CSRF habilitada e se não for feita a configuração
		 * ou o desligamento desta proteção, chamar endpoints com estes verbos HTTP resultará em
		 * falha na autorização HTTP 403.
		 */
		http.csrf(CsrfConfigurer::disable);
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
		 * A configuração a seguir é a uma outra forma de tratar as exceções de acesso não autorizado HTTP 401.
		 * Esta é uma configuração global que irá prover a resposta com base na configuração customizada que
		 * for atribuída.
		 */
		http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));
		/**
		 * A configuração de session fixation, determina que estratégia usar para proteger do ataque de session
		 * fixation ataque, onde um usuário pode "roubar" o cookie de um outro usuário e com isso ter detalhes
		 * pessoais associados a conta do usuário a qual a conta foi roubada.
		 */
		http.sessionManagement(smc -> smc.sessionFixation(sfc -> sfc.changeSessionId()));
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