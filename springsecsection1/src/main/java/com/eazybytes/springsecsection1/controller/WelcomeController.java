package com.eazybytes.springsecsection1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

	public WelcomeController() {
		// default constructor
	}

	@GetMapping("/welcome")
	public String sayWelcome() {
		return "Welcome to Spring Application with security";
	}
}