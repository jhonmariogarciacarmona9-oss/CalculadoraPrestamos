package com.calculadora.controller;

import com.calculadora.model.Cliente;
import com.calculadora.model.Prestamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class PrestamoController {

    @Autowired
    private JdbcTemplate jdbc;

    // ---------------------------------------------------------------
    // GET / — página principal de la calculadora
    // ---------------------------------------------------------------
    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        Prestamo prestamo = (Prestamo) session.getAttribute("prestamoActual");
        if (prestamo != null) {
            model.addAttribute("prestamo", prestamo);
        }
        Boolean pendiente = (Boolean) session.getAttribute("prestamoPendiente");
        model.addAttribute("prestamoPendiente", Boolean.TRUE.equals(pendiente));
        return "calculadora";
    }

    // ---------------------------------------------------------------
    // GET /login
    // ---------------------------------------------------------------
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ---------------------------------------------------------------
    // POST /calcular — solo calcula y guarda en sesión (sin DB aún)
    // ---------------------------------------------------------------
    @PostMapping("/calcular")
    public String calcular(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String documento,
            @RequestParam(defaultValue = "") String direccion,
            @RequestParam double monto,
            @RequestParam double tasa,
            @RequestParam int cuotas,
            @RequestParam String periodicidad,
            HttpSession session,
            Model model) {

        if (monto <= 0 || tasa <= 0 || cuotas <= 0) {
            model.addAttribute("error", "Monto, tasa y cuotas deben ser mayores a cero.");
            return "calculadora";
        }

        Prestamo.TipoCuota tipoCuota;
        switch (periodicidad) {
            case "SEMANAL":   tipoCuota = Prestamo.TipoCuota.SEMANAL;   break;
            case "QUINCENAL": tipoCuota = Prestamo.TipoCuota.QUINCENAL; break;
            default:          tipoCuota = Prestamo.TipoCuota.MENSUAL;   break;
        }

        Cliente cliente = new Cliente(nombre, apellido, documento, direccion);
        Prestamo prestamo = new Prestamo(cliente, monto, tasa, cuotas, tipoCuota);
        prestamo.calcular();

        // Guardar en sesión como "pendiente" (sin ID asignado, sin DB todavía)
        session.setAttribute("prestamoActual", prestamo);
        session.setAttribute("prestamoPendiente", true);
        return "redirect:/";
    }

    // ---------------------------------------------------------------
    // POST /confirmar — acepta el préstamo, asigna avalador y guarda en DB
    // ---------------------------------------------------------------
    @PostMapping("/confirmar")
    public String confirmar(
            @RequestParam String avaladoPor,
            HttpSession session) {

        Prestamo prestamo = (Prestamo) session.getAttribute("prestamoActual");
        if (prestamo == null) return "redirect:/";

        prestamo.setAvaladoPor(avaladoPor);
        session.removeAttribute("prestamoPendiente");

        try {
            guardarClienteYPrestamo(prestamo.getCliente(), prestamo);
        } catch (Exception ex) {
            System.err.println("Aviso BD al confirmar préstamo: " + ex.getMessage());
        }

        session.setAttribute("prestamoActual", prestamo);
        return "redirect:/prestamos";
    }

    // ---------------------------------------------------------------
    // POST /cancelar — rechaza el préstamo, limpia sesión
    // ---------------------------------------------------------------
    @PostMapping("/cancelar")
    public String cancelar(HttpSession session) {
        session.removeAttribute("prestamoActual");
        session.removeAttribute("prestamoPendiente");
        return "redirect:/";
    }

    // ---------------------------------------------------------------
    // GET /prestamos — lista de todos los préstamos activos y liquidados
    // ---------------------------------------------------------------
    @GetMapping("/prestamos")
    public String listaPrestamos(Model model) {
        List<Map<String, Object>> activos = new ArrayList<>();
        List<Map<String, Object>> liquidados = new ArrayList<>();

        try {
            String sql =
                "SELECT p.id, c.nombre, c.apellido, c.documento, " +
                "p.monto_prestado, p.tasa_porcentaje, p.numero_cuotas, p.tipo_cuota, " +
                "p.valor_cuota, p.total_a_pagar, p.saldo_restante, p.cuotas_pagadas, " +
                "p.estado, p.avala_por " +
                "FROM prestamos p JOIN clientes c ON p.cliente_id = c.id " +
                "ORDER BY p.id DESC";

            List<Map<String, Object>> todos = jdbc.queryForList(sql);
            for (Map<String, Object> row : todos) {
                String estado = row.get("estado") != null ? row.get("estado").toString() : "";
                if ("LIQUIDADO".equals(estado)) {
                    liquidados.add(row);
                } else {
                    activos.add(row);
                }
            }
        } catch (Exception ex) {
            System.err.println("Aviso BD al listar préstamos: " + ex.getMessage());
            model.addAttribute("errorBD", ex.getMessage());
        }

        model.addAttribute("activos", activos);
        model.addAttribute("liquidados", liquidados);
        return "prestamos";
    }

    // ---------------------------------------------------------------
    // POST /registrar-pago
    // ---------------------------------------------------------------
    @PostMapping("/registrar-pago")
    public String registrarPago(HttpSession session) {
        Prestamo prestamo = (Prestamo) session.getAttribute("prestamoActual");
        if (prestamo != null && !prestamo.estaLiquidado()) {
            prestamo.registrarPago();
            try {
                guardarPago(prestamo);
            } catch (Exception ex) {
                System.err.println("Aviso BD al guardar pago: " + ex.getMessage());
            }
            session.setAttribute("prestamoActual", prestamo);
        }
        return "redirect:/";
    }

    // ---------------------------------------------------------------
    // POST /nuevo — limpia la sesión para un nuevo préstamo
    // ---------------------------------------------------------------
    @PostMapping("/nuevo")
    public String nuevo(HttpSession session) {
        session.removeAttribute("prestamoActual");
        session.removeAttribute("prestamoPendiente");
        return "redirect:/";
    }

    // ----------------------------------------------------------------
    // Operaciones BD
    // ----------------------------------------------------------------

    private void guardarClienteYPrestamo(Cliente cliente, Prestamo prestamo) throws Exception {
        String sqlCliente =
            "INSERT INTO clientes (nombre, apellido, documento, direccion) " +
            "VALUES (?, ?, ?, ?) " +
            "ON CONFLICT (documento) DO UPDATE SET " +
            "nombre = EXCLUDED.nombre, apellido = EXCLUDED.apellido, direccion = EXCLUDED.direccion " +
            "RETURNING id";

        Integer clienteId = jdbc.queryForObject(sqlCliente, Integer.class,
            cliente.getNombre(), cliente.getApellido(),
            cliente.getDocumento(), cliente.getDireccion());
        cliente.setId(clienteId);

        String sqlPrestamo =
            "INSERT INTO prestamos " +
            "(cliente_id, monto_prestado, tasa_porcentaje, numero_cuotas, tipo_cuota, " +
            "valor_cuota, total_a_pagar, saldo_restante, cuotas_pagadas, estado, avala_por) " +
            "VALUES (?, ?, ?, ?, ?::tipo_cuota_enum, ?, ?, ?, 0, 'ACTIVO', ?) RETURNING id";

        Integer prestamoId = jdbc.queryForObject(sqlPrestamo, Integer.class,
            clienteId,
            prestamo.getMontoPrestado(),
            prestamo.getTasaPorcentaje(),
            prestamo.getNumeroCuotas(),
            prestamo.getTipoCuota().name(),
            prestamo.getValorCuota(),
            prestamo.getTotalAPagar(),
            prestamo.getSaldoRestante(),
            prestamo.getAvaladoPor());
        prestamo.setId(prestamoId);
    }

    private void guardarPago(Prestamo prestamo) throws Exception {
        jdbc.update(
            "INSERT INTO pagos (prestamo_id, numero_cuota, valor_pagado, saldo_restante) VALUES (?, ?, ?, ?)",
            prestamo.getId(),
            prestamo.getCuotasPagadas(),
            prestamo.getValorCuota(),
            prestamo.getSaldoRestante());

        String estadoNuevo = prestamo.estaLiquidado() ? "LIQUIDADO" : "ACTIVO";
        jdbc.update(
            "UPDATE prestamos SET saldo_restante = ?, cuotas_pagadas = ?, estado = ?::estado_prestamo_enum WHERE id = ?",
            prestamo.getSaldoRestante(),
            prestamo.getCuotasPagadas(),
            estadoNuevo,
            prestamo.getId());
    }
}
