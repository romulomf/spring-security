package com.eazybytes.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.AccountTransactions;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.AccountTransactionsRepository;
import com.eazybytes.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BalanceController {

	private final AccountTransactionsRepository accountTransactionsRepository;

	private final CustomerRepository customerRepository;

	@GetMapping("/myBalance")
	public List<AccountTransactions> getBalanceDetails(@RequestParam String email) {
		Optional<Customer> optionalCustomer = customerRepository.findByEmail(email);
		if (optionalCustomer.isPresent()) {
			List<AccountTransactions> accountTransactions = accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(optionalCustomer.get().getCustomerId());
			if (accountTransactions != null) {
				return accountTransactions;
			} else {
				return null;
			}
		}
		return null;
	}
}