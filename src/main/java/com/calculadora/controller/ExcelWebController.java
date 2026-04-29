package com.calculadora.controller;

import com.calculadora.model.Prestamo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;

@Controller
public class ExcelWebController {

    private static final MediaType EXCEL_TYPE =
        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    // ---------------------------------------------------------------
    // GET /exportar-historial
    // ---------------------------------------------------------------
    @GetMapping("/exportar-historial")
    public ResponseEntity<byte[]> exportarHistorial(HttpSession session) {
        Prestamo prestamo = (Prestamo) session.getAttribute("prestamoActual");
        if (prestamo == null) {
            return ResponseEntity.badRequest().build();
        }
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Historial");
            CellStyle titulo  = estiloTitulo(wb);
            CellStyle header  = estiloHeader(wb);
            CellStyle normal  = estiloNormal(wb);

            int fila = 0;

            // Título
            Row rowT = sheet.createRow(fila++);
            Cell cT = rowT.createCell(0);
            cT.setCellValue("Sistema de Préstamos — Historial de Pagos");
            cT.setCellStyle(titulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            fila++;

            // Info cliente
            addInfo(sheet, fila++, "Cliente:",        prestamo.getCliente().getNombreCompleto(), header, normal);
            addInfo(sheet, fila++, "Documento:",      prestamo.getCliente().getDocumento(), header, normal);
            addInfo(sheet, fila++, "Dirección:",      prestamo.getCliente().getDireccion(), header, normal);
            fila++;
            addInfo(sheet, fila++, "Monto prestado:", String.format("$ %,.2f", prestamo.getMontoPrestado()), header, normal);
            addInfo(sheet, fila++, "Tasa por cuota:", prestamo.getTasaPorcentaje() + "%", header, normal);
            addInfo(sheet, fila++, "Periodicidad:",   prestamo.getTipoCuota().toString(), header, normal);
            addInfo(sheet, fila++, "N° de cuotas:",   String.valueOf(prestamo.getNumeroCuotas()), header, normal);
            addInfo(sheet, fila++, "Valor de cuota:", String.format("$ %,.2f", prestamo.getValorCuota()), header, normal);
            addInfo(sheet, fila++, "Total a pagar:",  String.format("$ %,.2f", prestamo.getTotalAPagar()), header, normal);
            addInfo(sheet, fila++, "Total intereses:", String.format("$ %,.2f", prestamo.getTotalInteres()), header, normal);
            fila++;

            // Encabezados tabla
            String[] cols = {"N° Cuota", "Periodicidad", "Valor Pagado", "Saldo Restante", "Fecha de Pago"};
            Row rowH = sheet.createRow(fila++);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowH.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }

            // Datos
            for (String[] pago : prestamo.getHistorial()) {
                Row r = sheet.createRow(fila++);
                r.createCell(0).setCellValue(pago[0]);
                r.createCell(1).setCellValue(prestamo.getTipoCuota().toString());
                r.createCell(2).setCellValue("$ " + pago[1]);
                r.createCell(3).setCellValue("$ " + pago[2]);
                r.createCell(4).setCellValue(pago[3]);
                for (int c = 0; c < 5; c++) r.getCell(c).setCellStyle(normal);
            }

            for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            String filename = "historial_" + prestamo.getCliente().getDocumento() + ".xlsx";
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(EXCEL_TYPE)
                .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ---- Helpers estilos ----

    private CellStyle estiloTitulo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 14);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle estiloHeader(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle estiloNormal(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private void addInfo(Sheet sheet, int rowNum, String label, String value, CellStyle lStyle, CellStyle vStyle) {
        Row r = sheet.createRow(rowNum);
        Cell cl = r.createCell(0); cl.setCellValue(label); cl.setCellStyle(lStyle);
        Cell cv = r.createCell(1); cv.setCellValue(value); cv.setCellStyle(vStyle);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
    private int toInt(Object o) { return o == null ? 0 : ((Number) o).intValue(); }
    private double toDouble(Object o) { return o == null ? 0.0 : ((Number) o).doubleValue(); }
}
