package com.eazybytes.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final CustomerRepository customerRepository;

	@GetMapping("/user")
	public Customer getUserDetailsAfterLogin(Authentication authentication) {
		Optional<Customer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
		return optionalCustomer.orElse(null);
	}
}