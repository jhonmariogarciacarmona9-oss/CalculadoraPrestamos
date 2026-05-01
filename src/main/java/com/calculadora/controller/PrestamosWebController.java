package com.calculadora.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.calculadora.model.Cliente;
import com.calculadora.model.Prestamo;
import com.calculadora.model.Prestamo.TipoCuota;

@Controller
@SessionAttributes({"prestamo", "prestamoPendiente"})
public class PrestamosWebController {
    @GetMapping("/prestamos")
    public String prestamos(Model model, @org.springframework.web.bind.annotation.ModelAttribute("prestamo") Prestamo prestamo) {
        // Aquí deberías cargar los préstamos desde la base de datos o servicio
        // model.addAttribute("activos", ...);
        // model.addAttribute("liquidados", ...);
        return "prestamos";
    }

    @PostMapping("/nuevo")
    public String nuevoPrestamo(SessionStatus status, Model model) {
        status.setComplete(); // Limpia la sesión
        model.addAttribute("prestamo", null);
        model.addAttribute("prestamoPendiente", false);
        return "calculadora";
    }

    @PostMapping("/calcular")
    public String calcularPrestamo(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String documento,
            @RequestParam(required = false) String direccion,
            @RequestParam double monto,
            @RequestParam double tasa,
            @RequestParam int cuotas,
            @RequestParam String periodicidad,
            Model model) {
        Cliente cliente = new Cliente(nombre, apellido, documento, direccion);
        Prestamo prestamo = new Prestamo(cliente, monto, tasa, cuotas, TipoCuota.valueOf(periodicidad));
        prestamo.calcular();
        model.addAttribute("prestamo", prestamo);
        model.addAttribute("prestamoPendiente", true);
        return "calculadora";
    }

    @PostMapping("/registrar-pago")
    public String registrarPago(@org.springframework.web.bind.annotation.ModelAttribute("prestamo") Prestamo prestamo, Model model) {
        if (prestamo != null && !prestamo.estaLiquidado()) {
            prestamo.registrarPago();
        }
        model.addAttribute("prestamoPendiente", false);
        model.addAttribute("prestamo", prestamo);
        return "calculadora";
    }

    @PostMapping("/cancelar")
    public String cancelarPrestamo(SessionStatus status, Model model) {
        status.setComplete();
        model.addAttribute("prestamo", null);
        model.addAttribute("prestamoPendiente", false);
        return "calculadora";
    }

    @PostMapping("/confirmar")
    public String confirmarPrestamo(@RequestParam String avaladoPor, @org.springframework.web.bind.annotation.ModelAttribute("prestamo") Prestamo prestamo, Model model) {
        if (prestamo != null) {
            prestamo.setAvaladoPor(avaladoPor);
        }
        model.addAttribute("prestamoPendiente", false);
        model.addAttribute("prestamo", prestamo);
        return "calculadora";
    }

    @GetMapping("/exportar-historial")
    public String exportarHistorial(@org.springframework.web.bind.annotation.ModelAttribute("prestamo") Prestamo prestamo, Model model) {
        // Aquí podrías exportar el historial a Excel si lo deseas
        // Por ahora solo recarga la página
        model.addAttribute("prestamo", prestamo);
        return "calculadora";
    }
}
