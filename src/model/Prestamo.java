package model;

import java.util.ArrayList;
import java.util.List;

public class Prestamo {

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

    private int id;
    private Cliente cliente;
    private double montoPrestado;
    private double tasaPorcentaje;
    private int numeroCuotas;
    private TipoCuota tipoCuota;

    // Calculados
    private double valorCuota;
    private double totalAPagar;
    private double saldoRestante;
    private int cuotasPagadas;
    private String estado; // "ACTIVO" | "LIQUIDADO"

    // Historial en memoria: {nCuota, valorPagado, saldoRestante, fecha}
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

    /** Calcula valorCuota, totalAPagar e inicializa saldoRestante. */
    public void calcular() {
        double totalInteres = montoPrestado * (tasaPorcentaje / 100.0) * numeroCuotas;
        totalAPagar = montoPrestado + totalInteres;
        valorCuota = totalAPagar / numeroCuotas;
        saldoRestante = totalAPagar;
    }

    /** Registra el pago de una cuota y actualiza el saldo. */
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
            new java.util.Date().toString()
        };
        historial.add(fila);
    }

    public boolean estaLiquidado() {
        return saldoRestante <= 0.01;
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

    public double getTotalInteres() {
        return totalAPagar - montoPrestado;
    }
}
