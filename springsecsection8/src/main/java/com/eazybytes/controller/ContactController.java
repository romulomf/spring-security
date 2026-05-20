package com.eazybytes.controller;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.Contact;
import com.eazybytes.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ContactController {

	private final ContactRepository contactRepository;

	@GetMapping("/contact")
	public Contact saveContactInquiryDetails(@RequestBody Contact contact) {
		contact.setContactId(getServiceReqNumber());
		contact.setCreateDt(Date.from(Instant.now()));
		return contactRepository.save(contact);
	}

	public String getServiceReqNumber() {
		Random random = new Random();
		int ranNum = random.nextInt(999999999 - 9999) + 9999;
		return "SR" + ranNum;
	}
}