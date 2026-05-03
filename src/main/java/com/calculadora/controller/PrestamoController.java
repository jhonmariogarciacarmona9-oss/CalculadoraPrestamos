package com.calculadora.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calculadora.model.Prestamo;
import com.calculadora.repository.PrestamoRepository;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

	private final PrestamoRepository prestamoRepository;

	public PrestamoController(PrestamoRepository prestamoRepository) {
		this.prestamoRepository = prestamoRepository;
	}

	@GetMapping
	public List<Prestamo> listarPrestamos() {
		return prestamoRepository.findAll();
	}

	@PostMapping
	public Prestamo crearPrestamo(@RequestBody Prestamo prestamo) {
		prestamo.calcular();
		return prestamoRepository.save(prestamo);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Prestamo> obtenerPrestamo(@PathVariable int id) {
		return prestamoRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/pago")
	public ResponseEntity<Prestamo> registrarPago(@PathVariable int id) {
		return prestamoRepository.findById(id).map(p -> {
			p.registrarPago();
			return ResponseEntity.ok(prestamoRepository.save(p));
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarPrestamo(@PathVariable int id) {
		if (!prestamoRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		prestamoRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
