package com.eazybytes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController {

	public ContactController() {
		// default constructor
	}

	@GetMapping("/contact")
	public String saveContactInquiryDetails() {
		return "Inquiry details are saved to the DB";
	}
}