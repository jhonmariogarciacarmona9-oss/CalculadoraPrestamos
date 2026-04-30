package com.calculadora.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/excel")
public class ExcelWebController {

	@GetMapping("/exportar")
	public String exportar() {
		// Simulación de exportación
		return "Exportación realizada (simulada)";
	}
}
