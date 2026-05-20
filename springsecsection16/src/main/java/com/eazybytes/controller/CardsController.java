package com.eazybytes.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.Cards;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.CardsRepository;
import com.eazybytes.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CardsController {

	private final CardsRepository cardsRepository;

	private final CustomerRepository customerRepository;

	@GetMapping("/myCards")
	public List<Cards> getCardsDetails(@RequestParam String email) {
		Optional<Customer> optionalCustomer = customerRepository.findByEmail(email);
		if (optionalCustomer.isPresent()) {
			List<Cards> cards = cardsRepository.findByCustomerId(optionalCustomer.get().getCustomerId());
			if (cards != null) {
				return cards;
			} else {
				return null;
			}
		}
		return null;
	}
}