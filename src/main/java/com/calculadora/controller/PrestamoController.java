package com.calculadora.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calculadora.model.Prestamo;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {
	private final Map<Integer, Prestamo> prestamos = new HashMap<>();
	private int nextId = 1;

	@GetMapping
	public List<Prestamo> listarPrestamos() {
		return new ArrayList<>(prestamos.values());
	}

	@PostMapping
	public Prestamo crearPrestamo(@RequestBody Prestamo prestamo) {
		prestamo.setId(nextId++);
		prestamo.calcular();
		prestamos.put(prestamo.getId(), prestamo);
		return prestamo;
	}

	@GetMapping("/{id}")
	public Prestamo obtenerPrestamo(@PathVariable int id) {
		return prestamos.get(id);
	}

	@PostMapping("/{id}/pago")
	public Prestamo registrarPago(@PathVariable int id) {
		Prestamo p = prestamos.get(id);
		if (p != null) {
			p.registrarPago();
		}
		return p;
	}

	@DeleteMapping("/{id}")
	public void eliminarPrestamo(@PathVariable int id) {
		prestamos.remove(id);
	}
}
