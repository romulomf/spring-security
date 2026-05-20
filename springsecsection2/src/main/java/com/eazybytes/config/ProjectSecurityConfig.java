package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectSecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) {
		http.authorizeHttpRequests(req -> req
			.requestMatchers("/myAccount", "/myBalance", "/myCards", "/myLoans").authenticated()
			.requestMatchers("/contact", "/error", "/notices").permitAll()
		);
		http.formLogin(withDefaults());
		http.httpBasic(withDefaults());
		return http.build();
	}
}