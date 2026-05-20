package com.eazybytes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

	public BalanceController() {
		// default constructor
	}

	@GetMapping("/myBalance")
	public String getBalanceDetails() {
		return "Here are the balance details from the DB";
	}
}