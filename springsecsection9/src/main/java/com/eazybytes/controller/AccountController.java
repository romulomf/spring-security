package com.eazybytes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.Accounts;
import com.eazybytes.repository.AccountsRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {

	private final AccountsRepository accountsRepository;

	@GetMapping("/myAccount")
	public Accounts getAccountDetails(@RequestParam long id) {
		Accounts accounts = accountsRepository.findByCustomerId(id);
		if (accounts != null) {
			return accounts;
		} else {
			return null;
		}
	}
}