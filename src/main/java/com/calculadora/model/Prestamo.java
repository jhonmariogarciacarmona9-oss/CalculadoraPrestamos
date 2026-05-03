package com.calculadora.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "prestamos")
public class Prestamo implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TipoCuota {
        SEMANAL, QUINCENAL, MENSUAL;

        @Override
        public String toString() {
            switch (this) {
                case SEMANAL:   return "Semanal";
                case QUINCENAL: return "Quincenal";
                case MENSUAL:   return "Mensual";
                default:        return name();
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "monto_prestado", nullable = false)
    private double montoPrestado;

    @Column(name = "tasa_porcentaje", nullable = false)
    private double tasaPorcentaje;

    @Column(name = "numero_cuotas", nullable = false)
    private int numeroCuotas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuota", nullable = false)
    private TipoCuota tipoCuota;

    @Column(name = "avalado_por")
    private String avaladoPor;

    @Column(name = "valor_cuota")
    private double valorCuota;

    @Column(name = "total_a_pagar")
    private double totalAPagar;

    @Column(name = "saldo_restante")
    private double saldoRestante;

    @Column(name = "cuotas_pagadas")
    private int cuotasPagadas;

    @Column(name = "estado")
    private String estado; // "ACTIVO" | "LIQUIDADO"

    // Historial en memoria — no persisted
    @Transient
    private List<String[]> historial = new ArrayList<>();

    public Prestamo() {}

    public Prestamo(Cliente cliente, double montoPrestado, double tasaPorcentaje,
                    int numeroCuotas, TipoCuota tipoCuota) {
        this.cliente = cliente;
        this.montoPrestado = montoPrestado;
        this.tasaPorcentaje = tasaPorcentaje;
        this.numeroCuotas = numeroCuotas;
        this.tipoCuota = tipoCuota;
        this.estado = "ACTIVO";
        this.cuotasPagadas = 0;
    }

    public void calcular() {
        double totalInteres = montoPrestado * (tasaPorcentaje / 100.0) * numeroCuotas;
        totalAPagar = montoPrestado + totalInteres;
        valorCuota = totalAPagar / numeroCuotas;
        saldoRestante = totalAPagar;
    }

    public void registrarPago() {
        if (estaLiquidado()) return;
        cuotasPagadas++;
        saldoRestante -= valorCuota;
        if (saldoRestante < 0.01) {
            saldoRestante = 0;
            estado = "LIQUIDADO";
        }
        String[] fila = {
            String.valueOf(cuotasPagadas),
            String.format("%.2f", valorCuota),
            String.format("%.2f", saldoRestante),
            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())
        };
        historial.add(fila);
    }

    public boolean estaLiquidado() {
        return saldoRestante <= 0.01;
    }

    public double getTotalInteres() {
        return totalAPagar - montoPrestado;
    }

    // --- Getters / Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public double getMontoPrestado() { return montoPrestado; }
    public void setMontoPrestado(double montoPrestado) { this.montoPrestado = montoPrestado; }

    public double getTasaPorcentaje() { return tasaPorcentaje; }
    public void setTasaPorcentaje(double tasaPorcentaje) { this.tasaPorcentaje = tasaPorcentaje; }

    public int getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(int numeroCuotas) { this.numeroCuotas = numeroCuotas; }

    public TipoCuota getTipoCuota() { return tipoCuota; }
    public void setTipoCuota(TipoCuota tipoCuota) { this.tipoCuota = tipoCuota; }

    public String getAvaladoPor() { return avaladoPor; }
    public void setAvaladoPor(String avaladoPor) { this.avaladoPor = avaladoPor; }

    public double getValorCuota() { return valorCuota; }
    public void setValorCuota(double valorCuota) { this.valorCuota = valorCuota; }

    public double getTotalAPagar() { return totalAPagar; }
    public void setTotalAPagar(double totalAPagar) { this.totalAPagar = totalAPagar; }

    public double getSaldoRestante() { return saldoRestante; }
    public void setSaldoRestante(double saldoRestante) { this.saldoRestante = saldoRestante; }

    public int getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(int cuotasPagadas) { this.cuotasPagadas = cuotasPagadas; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<String[]> getHistorial() { return historial; }
    public void setHistorial(List<String[]> historial) { this.historial = historial; }
}
