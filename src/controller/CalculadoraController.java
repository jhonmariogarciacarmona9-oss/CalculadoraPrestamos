package controller;

import db.ConexionDB;
import model.Cliente;
import model.Prestamo;
import view.CalculadoraView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CalculadoraController {

    private final CalculadoraView view;
    private Prestamo prestamoActual;

    public CalculadoraController(CalculadoraView view) {
        this.view = view;
        inicializarListeners();
    }

    private void inicializarListeners() {
        view.getBtnCalcular().addActionListener(this::calcularPrestamo);
        view.getBtnRegistrarPago().addActionListener(this::registrarPago);
        view.getBtnExportarHistorial().addActionListener(e -> {
            if (prestamoActual != null) {
                ExcelController.exportarHistorialActual(prestamoActual, view);
            }
        });
        view.getBtnExportarReporte().addActionListener(e ->
            ExcelController.exportarReporteGeneral(view));
    }

    private void calcularPrestamo(ActionEvent e) {
        // Validar campos
        String nombre    = view.getTxtNombre();
        String apellido  = view.getTxtApellido();
        String documento = view.getTxtDocumento();
        String direccion = view.getTxtDireccion();
        String montoStr  = view.getTxtMonto();
        String tasaStr   = view.getTxtTasa();
        String cuotasStr = view.getTxtCuotas();
        String periodStr = view.getPeriodicidad();

        if (nombre.isEmpty() || apellido.isEmpty() || documento.isEmpty()) {
            view.mostrarMensaje("Datos incompletos", "Por favor complete los datos del cliente.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double monto, tasa;
        int cuotas;
        try {
            monto  = Double.parseDouble(montoStr);
            tasa   = Double.parseDouble(tasaStr);
            cuotas = Integer.parseInt(cuotasStr);
        } catch (NumberFormatException ex) {
            view.mostrarMensaje("Error de entrada", "Monto, tasa y cuotas deben ser números válidos.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (monto <= 0 || tasa <= 0 || cuotas <= 0) {
            view.mostrarMensaje("Valores inválidos", "Monto, tasa y cuotas deben ser mayores a cero.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Prestamo.TipoCuota tipoCuota;
        switch (periodStr) {
            case "Semanal":   tipoCuota = Prestamo.TipoCuota.SEMANAL;   break;
            case "Quincenal": tipoCuota = Prestamo.TipoCuota.QUINCENAL; break;
            default:          tipoCuota = Prestamo.TipoCuota.MENSUAL;   break;
        }

        // Crear cliente y préstamo
        Cliente cliente = new Cliente(nombre, apellido, documento, direccion);
        prestamoActual  = new Prestamo(cliente, monto, tasa, cuotas, tipoCuota);
        prestamoActual.calcular();

        // Persistir en BD
        try {
            guardarClienteYPrestamo(cliente, prestamoActual);
        } catch (SQLException ex) {
            view.mostrarMensaje("Aviso BD",
                "Préstamo calculado, pero no se pudo guardar en la base de datos:\n" + ex.getMessage(),
                JOptionPane.WARNING_MESSAGE);
        }

        // Actualizar vista
        view.limpiarHistorial();
        view.actualizarResultados(
            prestamoActual.getValorCuota(),
            prestamoActual.getTotalAPagar(),
            prestamoActual.getTotalInteres());
        view.actualizarSeguimiento(
            prestamoActual.getSaldoRestante(),
            0, cuotas);
        view.getBtnRegistrarPago().setEnabled(true);
        view.getBtnExportarHistorial().setEnabled(true);
        view.getBtnExportarReporte().setEnabled(true);
    }

    private void registrarPago(ActionEvent e) {
        if (prestamoActual == null || prestamoActual.estaLiquidado()) return;

        prestamoActual.registrarPago();

        // Agregar fila al historial en pantalla
        String[] fila = {
            String.valueOf(prestamoActual.getCuotasPagadas()),
            prestamoActual.getTipoCuota().toString(),
            String.format("$ %,.2f", prestamoActual.getValorCuota()),
            String.format("$ %,.2f", prestamoActual.getSaldoRestante()),
            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())
        };
        view.agregarFilaHistorial(fila);

        view.actualizarSeguimiento(
            prestamoActual.getSaldoRestante(),
            prestamoActual.getCuotasPagadas(),
            prestamoActual.getNumeroCuotas());

        // Persistir pago en BD
        try {
            guardarPago(prestamoActual);
        } catch (SQLException ex) {
            System.err.println("Error guardando pago en BD: " + ex.getMessage());
        }

        if (prestamoActual.estaLiquidado()) {
            view.getBtnRegistrarPago().setEnabled(false);
            String nombre = prestamoActual.getCliente().getNombreCompleto();
            view.mostrarMensaje(
                "¡Préstamo Liquidado!",
                "¡El cliente " + nombre + " ha terminado con el pago!\n\nSaldo final: $0.00",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ---- Operaciones BD ----

    private void guardarClienteYPrestamo(Cliente cliente, Prestamo prestamo) throws SQLException {
        Connection conn = ConexionDB.getInstance().getConexion();

        // Insertar o actualizar cliente por documento
        String sqlCliente = "INSERT INTO clientes (nombre, apellido, documento, direccion) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT (documento) DO UPDATE SET " +
                            "nombre = EXCLUDED.nombre, apellido = EXCLUDED.apellido, direccion = EXCLUDED.direccion " +
                            "RETURNING id";
        int clienteId;
        try (PreparedStatement ps = conn.prepareStatement(sqlCliente)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getDocumento());
            ps.setString(4, cliente.getDireccion());
            ResultSet rs = ps.executeQuery();
            rs.next();
            clienteId = rs.getInt(1);
            cliente.setId(clienteId);
        }

        // Insertar préstamo
        String sqlPrestamo = "INSERT INTO prestamos " +
            "(cliente_id, monto_prestado, tasa_porcentaje, numero_cuotas, tipo_cuota, " +
            "valor_cuota, total_a_pagar, saldo_restante, cuotas_pagadas, estado) " +
            "VALUES (?, ?, ?, ?, ?::tipo_cuota_enum, ?, ?, ?, 0, 'ACTIVO') RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sqlPrestamo)) {
            ps.setInt(1, clienteId);
            ps.setDouble(2, prestamo.getMontoPrestado());
            ps.setDouble(3, prestamo.getTasaPorcentaje());
            ps.setInt(4, prestamo.getNumeroCuotas());
            ps.setString(5, prestamo.getTipoCuota().name());
            ps.setDouble(6, prestamo.getValorCuota());
            ps.setDouble(7, prestamo.getTotalAPagar());
            ps.setDouble(8, prestamo.getSaldoRestante());
            ResultSet rs = ps.executeQuery();
            rs.next();
            prestamo.setId(rs.getInt(1));
        }
    }

    private void guardarPago(Prestamo prestamo) throws SQLException {
        Connection conn = ConexionDB.getInstance().getConexion();

        // Insertar pago
        String sqlPago = "INSERT INTO pagos (prestamo_id, numero_cuota, valor_pagado, saldo_restante) " +
                         "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlPago)) {
            ps.setInt(1, prestamo.getId());
            ps.setInt(2, prestamo.getCuotasPagadas());
            ps.setDouble(3, prestamo.getValorCuota());
            ps.setDouble(4, prestamo.getSaldoRestante());
            ps.executeUpdate();
        }

        // Actualizar saldo y estado en prestamos
        String estadoNuevo = prestamo.estaLiquidado() ? "LIQUIDADO" : "ACTIVO";
        String sqlUpdate = "UPDATE prestamos SET saldo_restante = ?, cuotas_pagadas = ?, estado = ?::estado_prestamo_enum WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            ps.setDouble(1, prestamo.getSaldoRestante());
            ps.setInt(2, prestamo.getCuotasPagadas());
            ps.setString(3, estadoNuevo);
            ps.setInt(4, prestamo.getId());
            ps.executeUpdate();
        }
    }
}
