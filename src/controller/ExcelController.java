package controller;

import db.ConexionDB;
import model.Prestamo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import view.CalculadoraView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelController {

    // ---------------------------------------------------------------
    // Exportar historial del préstamo activo
    // ---------------------------------------------------------------
    public static void exportarHistorialActual(Prestamo prestamo, CalculadoraView parentView) {
        File archivo = elegirArchivo(parentView, "historial_prestamo");
        if (archivo == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Historial");

            // Estilos
            CellStyle estiloTitulo = crearEstiloTitulo(wb);
            CellStyle estiloHeader = crearEstiloHeader(wb);
            CellStyle estiloNormal = crearEstiloNormal(wb);
            CellStyle estiloDinero = crearEstiloDinero(wb);

            int fila = 0;

            // Título
            Row rowTitulo = sheet.createRow(fila++);
            // ... (restaurar el resto del método y utilidades según la versión original)
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentView, "Error al exportar historial: " + ex.getMessage());
        }
    }

    // ... (restaurar otros métodos y utilidades según la versión original)
}
            Cell cTitulo = rowTitulo.createCell(0);
            cTitulo.setCellValue("Sistema de Préstamos — Historial de Pagos");
            cTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            fila++;

            // Datos del cliente
            agregarInfoFila(sheet, fila++, "Cliente:", prestamo.getCliente().getNombreCompleto(), estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "Documento:", prestamo.getCliente().getDocumento(), estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "Dirección:", prestamo.getCliente().getDireccion(), estiloHeader, estiloNormal);
            fila++;

            // Datos del préstamo
            agregarInfoFila(sheet, fila++, "Monto prestado:", String.format("$ %,.2f", prestamo.getMontoPrestado()), estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "Tasa por cuota:", prestamo.getTasaPorcentaje() + "%", estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "Periodicidad:", prestamo.getTipoCuota().toString(), estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "N° de cuotas:", String.valueOf(prestamo.getNumeroCuotas()), estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "Valor de cuota:", String.format("$ %,.2f", prestamo.getValorCuota()), estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "Total a pagar:", String.format("$ %,.2f", prestamo.getTotalAPagar()), estiloHeader, estiloNormal);
            agregarInfoFila(sheet, fila++, "Total intereses:", String.format("$ %,.2f", prestamo.getTotalInteres()), estiloHeader, estiloNormal);
            fila++;

            // Tabla de pagos
            String[] columnas = {"N° Cuota", "Periodicidad", "Valor Pagado", "Saldo Restante", "Fecha de Pago"};
            Row rowHeader = sheet.createRow(fila++);
            for (int i = 0; i < columnas.length; i++) {
                Cell c = rowHeader.createCell(i);
                c.setCellValue(columnas[i]);
                c.setCellStyle(estiloHeader);
            }

            for (String[] pago : prestamo.getHistorial()) {
                Row r = sheet.createRow(fila++);
                r.createCell(0).setCellValue(pago[0]);
                r.createCell(1).setCellValue(pago.length > 1 ? prestamo.getTipoCuota().toString() : "");
                r.createCell(2).setCellValue(pago[1]);
                r.createCell(3).setCellValue(pago[2]);
                r.createCell(4).setCellValue(pago[3]);
                for (int col = 0; col < 5; col++) r.getCell(col).setCellStyle(estiloNormal);
            }

            // Autoajuste
            for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                wb.write(fos);
            }

            JOptionPane.showMessageDialog(parentView,
                "Historial exportado exitosamente:\n" + archivo.getAbsolutePath(),
                "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentView,
                "Error al exportar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------
    // Exportar reporte general desde BD
    // ---------------------------------------------------------------
    public static void exportarReporteGeneral(CalculadoraView parentView) {
        File archivo = elegirArchivo(parentView, "reporte_general_prestamos");
        if (archivo == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte General");

            CellStyle estiloTitulo = crearEstiloTitulo(wb);
            CellStyle estiloHeader = crearEstiloHeader(wb);
            CellStyle estiloNormal = crearEstiloNormal(wb);

            int fila = 0;

            // Título
            Row rowTitulo = sheet.createRow(fila++);
            Cell cTitulo = rowTitulo.createCell(0);
            cTitulo.setCellValue("Sistema de Préstamos — Reporte General — " +
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            cTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            fila++;

            // Encabezados
            String[] cols = {
                "ID", "Cliente", "Documento", "Dirección",
                "Monto Prestado", "Tasa (%)", "N° Cuotas", "Periodicidad",
                "Valor Cuota", "Total a Pagar", "Saldo Restante", "Cuotas Pagadas", "Estado", "Fecha Creación"
            };
            Row rowH = sheet.createRow(fila++);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowH.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(estiloHeader);
            }

            // Datos desde BD
            Connection conn = ConexionDB.getInstance().getConexion();
            String sql = "SELECT p.id, c.nombre, c.apellido, c.documento, c.direccion, " +
                         "p.monto_prestado, p.tasa_porcentaje, p.numero_cuotas, p.tipo_cuota, " +
                         "p.valor_cuota, p.total_a_pagar, p.saldo_restante, p.cuotas_pagadas, " +
                         "p.estado, p.fecha_creacion " +
                         "FROM prestamos p JOIN clientes c ON p.cliente_id = c.id " +
                         "ORDER BY p.fecha_creacion DESC";

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    Row r = sheet.createRow(fila++);
                    r.createCell(0).setCellValue(rs.getInt("id"));
                    r.createCell(1).setCellValue(rs.getString("nombre") + " " + rs.getString("apellido"));
                    r.createCell(2).setCellValue(rs.getString("documento"));
                    r.createCell(3).setCellValue(rs.getString("direccion"));
                    r.createCell(4).setCellValue(rs.getDouble("monto_prestado"));
                    r.createCell(5).setCellValue(rs.getDouble("tasa_porcentaje"));
                    r.createCell(6).setCellValue(rs.getInt("numero_cuotas"));
                    r.createCell(7).setCellValue(rs.getString("tipo_cuota"));
                    r.createCell(8).setCellValue(rs.getDouble("valor_cuota"));
                    r.createCell(9).setCellValue(rs.getDouble("total_a_pagar"));
                    r.createCell(10).setCellValue(rs.getDouble("saldo_restante"));
                    r.createCell(11).setCellValue(rs.getInt("cuotas_pagadas"));
                    r.createCell(12).setCellValue(rs.getString("estado"));
                    Timestamp ts = rs.getTimestamp("fecha_creacion");
                    r.createCell(13).setCellValue(ts != null ? ts.toString() : "");
                    for (int col = 0; col < 14; col++) {
                        if (r.getCell(col) != null) r.getCell(col).setCellStyle(estiloNormal);
                    }
                }
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                wb.write(fos);
            }

            JOptionPane.showMessageDialog(parentView,
                "Reporte general exportado exitosamente:\n" + archivo.getAbsolutePath(),
                "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentView,
                "Error al exportar reporte: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Helpers ----

    private static File elegirArchivo(Component parent, String nombreSugerido) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar archivo Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File(nombreSugerido + "_" +
            new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".xlsx"));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return null;
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".xlsx")) {
            f = new File(f.getAbsolutePath() + ".xlsx");
        }
        return f;
    }

    private static void agregarInfoFila(Sheet sheet, int numFila, String clave, String valor,
                                         CellStyle estiloKey, CellStyle estiloVal) {
        Row r = sheet.createRow(numFila);
        Cell c0 = r.createCell(0); c0.setCellValue(clave); c0.setCellStyle(estiloKey);
        Cell c1 = r.createCell(1); c1.setCellValue(valor);  c1.setCellStyle(estiloVal);
    }

    private static CellStyle crearEstiloTitulo(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle crearEstiloHeader(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloNormal(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloDinero(Workbook wb) {
        CellStyle style = crearEstiloNormal(wb);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("$#,##0.00"));
        return style;
    }
}
