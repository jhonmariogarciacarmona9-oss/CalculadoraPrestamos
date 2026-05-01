package com.calculadora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {
	@GetMapping("/")
	public String home(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()) {
			return "calculadora"; // Vista principal para usuarios autenticados
		} else {
			return "redirect:/login";
		}
	}
}
