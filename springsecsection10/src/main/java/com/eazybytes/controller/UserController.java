package com.eazybytes.controller;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final CustomerRepository customerRepository;
	
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
		try {
			String hashPwd = passwordEncoder.encode(customer.getPwd());
			customer.setPwd(hashPwd);
			customer.setCreateDt(Date.from(Instant.now()));
			Customer savedCustomer = customerRepository.save(customer);
			if (savedCustomer.getCustomerId() > 0) {
				return ResponseEntity.status(HttpStatus.CREATED).body("Given user details are succesfully registered");
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User registration failed");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format("An exception occurred: %s", e.getMessage()));
		}
	}

	@GetMapping("/user")
	public Customer getUserDetailsAfterLogin(Authentication authentication) {
		Optional<Customer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
		return optionalCustomer.orElse(null);
	}
}