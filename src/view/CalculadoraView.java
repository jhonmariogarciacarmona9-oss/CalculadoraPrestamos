package view;

import model.Prestamo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CalculadoraView extends JFrame {

    // --- Datos del cliente ---
    private JTextField txtNombre, txtApellido, txtDocumento, txtDireccion;

    // --- Datos del préstamo ---
    private JTextField txtMonto, txtTasa, txtCuotas;
    private JComboBox<String> cmbPeriodicidad;
    private JButton btnCalcular;

    // --- Resultados ---
    private JLabel lblValorCuota, lblTotalPagar, lblTotalInteres;

    // --- Seguimiento ---
    private JLabel lblSaldoRestante, lblCuotasPagadas;
    private JProgressBar progressBar;
    private JButton btnRegistrarPago;

    // --- Historial ---
    private DefaultTableModel modeloTabla;
    private JTable tablaHistorial;

    // --- Exportar ---
    private JButton btnExportarHistorial, btnExportarReporte;

    public CalculadoraView() {
        setTitle("Calculadora de Préstamos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(720, 600));
        initComponents();
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        panelPrincipal.setBackground(new Color(248, 250, 252));

        // ---- Panel izquierdo (entrada) ----
        JPanel panelIzq = new JPanel();
        panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS));
        panelIzq.setBackground(new Color(248, 250, 252));
        panelIzq.setPreferredSize(new Dimension(370, 0));

        panelIzq.add(crearPanelCliente());
        panelIzq.add(Box.createVerticalStrut(10));
        panelIzq.add(crearPanelPrestamo());
        panelIzq.add(Box.createVerticalStrut(10));
        panelIzq.add(crearPanelResultados());
        panelIzq.add(Box.createVerticalStrut(10));
        panelIzq.add(crearPanelSeguimiento());
        panelIzq.add(Box.createVerticalGlue());

        // Botones de exportar
        JPanel panelExport = new JPanel(new GridLayout(1, 2, 8, 0));
        panelExport.setBackground(new Color(248, 250, 252));
        btnExportarHistorial = crearBoton("Exportar Historial", new Color(16, 185, 129));
        btnExportarReporte   = crearBoton("Exportar Reporte General", new Color(99, 102, 241));
        btnExportarHistorial.setEnabled(false);
        btnExportarReporte.setEnabled(false);
        panelExport.add(btnExportarHistorial);
        panelExport.add(btnExportarReporte);
        panelIzq.add(Box.createVerticalStrut(10));
        panelIzq.add(panelExport);

        // ---- Panel derecho (historial) ----
        JPanel panelDer = new JPanel(new BorderLayout(0, 8));
        panelDer.setBackground(new Color(248, 250, 252));

        JLabel lblHistorial = new JLabel("Historial de Pagos");
        lblHistorial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHistorial.setForeground(new Color(30, 41, 59));

        String[] columnas = {"N° Cuota", "Periodicidad", "Valor Pagado", "Saldo Restante", "Fecha"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaHistorial.setRowHeight(24);
        tablaHistorial.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaHistorial.getTableHeader().setBackground(new Color(226, 232, 240));
        tablaHistorial.setSelectionBackground(new Color(219, 234, 254));

        JScrollPane scroll = new JScrollPane(tablaHistorial);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        panelDer.add(lblHistorial, BorderLayout.NORTH);
        panelDer.add(scroll, BorderLayout.CENTER);

        panelPrincipal.add(panelIzq, BorderLayout.WEST);
        panelPrincipal.add(panelDer, BorderLayout.CENTER);
        setContentPane(panelPrincipal);
    }

    private JPanel crearPanelCliente() {
        JPanel panel = crearPanelTarjeta("Datos del Cliente");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(148, 163, 184)),
            "Datos del Cliente",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(30, 41, 59)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.weightx = 1;

        txtNombre    = new JTextField();
        txtApellido  = new JTextField();
        txtDocumento = new JTextField();
        txtDireccion = new JTextField();

        agregarFila(panel, gbc, "Nombre:",    txtNombre,    0);
        agregarFila(panel, gbc, "Apellido:",  txtApellido,  1);
        agregarFila(panel, gbc, "Documento:", txtDocumento, 2);
        agregarFila(panel, gbc, "Dirección:", txtDireccion, 3);

        return panel;
    }

    private JPanel crearPanelPrestamo() {
        JPanel panel = crearPanelTarjeta("Datos del Préstamo");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(148, 163, 184)),
            "Datos del Préstamo",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(30, 41, 59)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.weightx = 1;

        txtMonto  = new JTextField();
        txtTasa   = new JTextField();
        txtCuotas = new JTextField();
        cmbPeriodicidad = new JComboBox<>(new String[]{"Semanal", "Quincenal", "Mensual"});
        cmbPeriodicidad.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbPeriodicidad.setSelectedIndex(2);

        agregarFila(panel, gbc, "Monto ($):",        txtMonto,          0);
        agregarFila(panel, gbc, "Tasa por cuota (%):", txtTasa,          1);
        agregarFila(panel, gbc, "N° de cuotas:",     txtCuotas,         2);
        agregarFila(panel, gbc, "Periodicidad:",     cmbPeriodicidad,   3);

        btnCalcular = crearBoton("Calcular Préstamo", new Color(59, 130, 246));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(10, 6, 4, 6);
        panel.add(btnCalcular, gbc);

        return panel;
    }

    private JPanel crearPanelResultados() {
        JPanel panel = crearPanelTarjeta("Resultados");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(148, 163, 184)),
            "Resultados",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(30, 41, 59)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.weightx = 1;

        lblValorCuota  = crearLabelResultado("—");
        lblTotalPagar  = crearLabelResultado("—");
        lblTotalInteres = crearLabelResultado("—");

        agregarFilaLabel(panel, gbc, "Valor de cuota:", lblValorCuota,   0);
        agregarFilaLabel(panel, gbc, "Total a pagar:",  lblTotalPagar,   1);
        agregarFilaLabel(panel, gbc, "Total intereses:", lblTotalInteres, 2);

        return panel;
    }

    private JPanel crearPanelSeguimiento() {
        JPanel panel = crearPanelTarjeta("Seguimiento");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(148, 163, 184)),
            "Seguimiento de Pago",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(30, 41, 59)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.weightx = 1;

        lblSaldoRestante  = crearLabelResultado("—");
        lblCuotasPagadas  = crearLabelResultado("—");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        progressBar.setForeground(new Color(34, 197, 94));

        agregarFilaLabel(panel, gbc, "Saldo restante:",   lblSaldoRestante, 0);
        agregarFilaLabel(panel, gbc, "Cuotas pagadas:",   lblCuotasPagadas, 1);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(progressBar, gbc);

        btnRegistrarPago = crearBoton("Registrar Pago de Cuota", new Color(34, 197, 94));
        btnRegistrarPago.setEnabled(false);
        gbc.gridy = 3; gbc.insets = new Insets(10, 6, 4, 6);
        panel.add(btnRegistrarPago, gbc);

        return panel;
    }

    // ---- Helpers ----
    private JPanel crearPanelTarjeta(String titulo) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return p;
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, String labelTxt, JComponent campo, int fila) {
        JLabel lbl = new JLabel(labelTxt);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (campo instanceof JTextField) {
            ((JTextField) campo).setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ((JTextField) campo).setPreferredSize(new Dimension(160, 30));
            ((JTextField) campo).setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)));
        }
        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 1; gbc.weightx = 0.35;
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(campo, gbc);
    }

    private void agregarFilaLabel(JPanel panel, GridBagConstraints gbc, String texto, JLabel valor, int fila) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 1; gbc.weightx = 0.5;
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        panel.add(valor, gbc);
    }

    private JLabel crearLabelResultado(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 41, 59));
        return lbl;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(160, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---- Getters para el controlador ----
    public String getTxtNombre()    { return txtNombre.getText().trim(); }
    public String getTxtApellido()  { return txtApellido.getText().trim(); }
    public String getTxtDocumento() { return txtDocumento.getText().trim(); }
    public String getTxtDireccion() { return txtDireccion.getText().trim(); }
    public String getTxtMonto()     { return txtMonto.getText().trim(); }
    public String getTxtTasa()      { return txtTasa.getText().trim(); }
    public String getTxtCuotas()    { return txtCuotas.getText().trim(); }
    public String getPeriodicidad() { return (String) cmbPeriodicidad.getSelectedItem(); }

    public JButton getBtnCalcular()        { return btnCalcular; }
    public JButton getBtnRegistrarPago()   { return btnRegistrarPago; }
    public JButton getBtnExportarHistorial(){ return btnExportarHistorial; }
    public JButton getBtnExportarReporte() { return btnExportarReporte; }

    public DefaultTableModel getModeloTabla() { return modeloTabla; }

    public void actualizarResultados(double valorCuota, double totalPagar, double totalInteres) {
        lblValorCuota.setText(String.format("$ %,.2f", valorCuota));
        lblTotalPagar.setText(String.format("$ %,.2f", totalPagar));
        lblTotalInteres.setText(String.format("$ %,.2f", totalInteres));
    }

    public void actualizarSeguimiento(double saldoRestante, int pagadas, int total) {
        lblSaldoRestante.setText(String.format("$ %,.2f", saldoRestante));
        lblCuotasPagadas.setText(pagadas + " / " + total);
        int progreso = (total > 0) ? (int) ((pagadas * 100.0) / total) : 0;
        progressBar.setValue(progreso);
        progressBar.setString(progreso + "%");
    }

    public void agregarFilaHistorial(String[] fila) {
        modeloTabla.addRow(fila);
        int ultima = tablaHistorial.getRowCount() - 1;
        tablaHistorial.scrollRectToVisible(tablaHistorial.getCellRect(ultima, 0, true));
    }

    public void limpiarHistorial() {
        modeloTabla.setRowCount(0);
    }

    public void mostrarMensaje(String titulo, String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }
}
