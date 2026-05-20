package com.eazybytes.events;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationEvents {

	@EventListener
	public void onSuccess(AuthenticationSuccessEvent e) {
		log.info("Login successful for the user: {}", e.getAuthentication().getName());
	}

	@EventListener
	public void onFailure(AbstractAuthenticationFailureEvent e) {
		log.error("Login failed for the user: {} due to: {}", e.getAuthentication().getName(), e.getException().getMessage());
	}
}