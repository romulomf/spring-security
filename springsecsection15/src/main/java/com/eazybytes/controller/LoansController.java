package com.eazybytes.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.Customer;
import com.eazybytes.model.Loans;
import com.eazybytes.repository.CustomerRepository;
import com.eazybytes.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LoansController {

	private final LoanRepository loanRepository;

	private final CustomerRepository customerRepository;

	@GetMapping("/myLoans")
	@PostAuthorize("hasRole('USER')")
	public List<Loans> getLoansDetails(@RequestParam String email) {
		Optional<Customer> optionalCustomer = customerRepository.findByEmail(email);
		if (optionalCustomer.isPresent()) {
			List<Loans> loans = loanRepository.findByCustomerIdOrderByStartDtDesc(optionalCustomer.get().getCustomerId());
			if (loans != null) {
				return loans;
			} else {
				return null;
			}
		}
		return null;
	}
}