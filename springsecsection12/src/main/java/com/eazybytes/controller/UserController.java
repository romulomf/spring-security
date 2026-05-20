package com.eazybytes.controller;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.constants.ApplicationConstants;
import com.eazybytes.model.Customer;
import com.eazybytes.model.LoginRequestDTO;
import com.eazybytes.model.LoginResponseDTO;
import com.eazybytes.repository.CustomerRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final CustomerRepository customerRepository;
	
	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final Environment environment;

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

	@PostMapping("/apiLogin")
	public ResponseEntity<LoginResponseDTO> apiLogin(@RequestBody LoginRequestDTO loginRequest) {
		String jwt = "";
		Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(), loginRequest.password());
		Authentication authenticationResponse = authenticationManager.authenticate(authentication);
		if (authenticationResponse != null && authenticationResponse.isAuthenticated()) {
			String secret = environment.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
			SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
			jwt = Jwts.builder()
					.issuer("Eazy Bank")
					.subject("JWT Token")
					.claim("username", authenticationResponse.getName())
					.claim("authorities", authenticationResponse.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
					.issuedAt(Date.from(Instant.now()))
					.expiration(Date.from(Instant.now().plus(8, ChronoUnit.HOURS)))
					.signWith(secretKey)
					.compact();
		}
		return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstants.JWT_HEADER, jwt).body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt));
	}
}