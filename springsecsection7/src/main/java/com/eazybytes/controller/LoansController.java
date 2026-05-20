package com.eazybytes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoansController {

	public LoansController() {
		// default constructor
	}

	@GetMapping("/myLoans")
	public String getLoansDetails() {
		return "Here are the loans details from the DB";
	}
}