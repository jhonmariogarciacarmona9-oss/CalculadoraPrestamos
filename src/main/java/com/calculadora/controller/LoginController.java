package com.calculadora.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {

	@PostMapping
	public boolean login(@RequestParam String usuario, @RequestParam String password) {
		// Simulación de autenticación
		return "admin".equals(usuario) && "admin".equals(password);
	}
}
