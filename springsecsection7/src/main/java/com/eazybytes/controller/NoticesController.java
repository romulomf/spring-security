package com.eazybytes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticesController {

	public NoticesController() {
		// default constructor
	}

	@GetMapping("/notices")
	public String getNotices() {
		return "Here are the notices from the DB";
	}
}