package com.mymart.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.mymart.model.Order;
import com.mymart.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final OrderRepository orderRepository;
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZONE);

    public List<Order> getOrdersForRange(LocalDate from, LocalDate to) {
        Instant start = from.atStartOfDay(ZONE).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(ZONE).toInstant();
        return orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(start, end);
    }

    public byte[] exportSalesCsv(LocalDate from, LocalDate to) {
        List<Order> orders = getOrdersForRange(from, to);

        try (StringWriter sw = new StringWriter();
             CSVWriter writer = new CSVWriter(sw)) {

            // Header
            writer.writeNext(new String[]{
                    "Order Number", "Order Date", "Customer Name", "Customer Email", "Customer Phone",
                    "Items Count", "Original Total (INR)", "Discount Total (INR)", "Delivery Charge (INR)",
                    "Tax (GST INR)", "Final Amount (INR)", "Payment Method", "Payment Status", "Order Status"
            });

            double totalSales = 0.0;
            for (Order o : orders) {
                totalSales += o.getFinalTotal() != null ? o.getFinalTotal() : 0.0;
                writer.writeNext(new String[]{
                        o.getOrderNumber(),
                        o.getCreatedAt() != null ? DATE_FMT.format(o.getCreatedAt()) : "",
                        o.getCustomerSnapshot() != null ? o.getCustomerSnapshot().getName() : "",
                        o.getCustomerSnapshot() != null ? o.getCustomerSnapshot().getEmail() : "",
                        o.getCustomerSnapshot() != null ? o.getCustomerSnapshot().getPhone() : "",
                        String.valueOf(o.getItems() != null ? o.getItems().size() : 0),
                        String.format("%.2f", o.getOriginalTotal()),
                        String.format("%.2f", o.getDiscountTotal()),
                        String.format("%.2f", o.getDeliveryCharge()),
                        String.format("%.2f", o.getTaxTotal()),
                        String.format("%.2f", o.getFinalTotal()),
                        o.getPaymentMethod(),
                        o.getPaymentStatus(),
                        o.getOrderStatus()
                });
            }

            // Summary row
            writer.writeNext(new String[]{
                    "TOTALS", "", "", "", "", "", "", "", "", "",
                    String.format("%.2f", totalSales), "", "", ""
            });

            return sw.toString().getBytes();
        } catch (Exception e) {
            log.error("Failed to export sales CSV: ", e);
            throw new RuntimeException("Error generating CSV: " + e.getMessage());
        }
    }

    public byte[] exportSalesExcel(LocalDate from, LocalDate to) {
        List<Order> orders = getOrdersForRange(from, to);

        try (org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("MYMART Sales Report");

            // Header Style
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title Row
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("MYMART Supermarket Sales Report (" + from + " to " + to + ")");

            String[] columns = {
                    "Order Number", "Order Date", "Customer Name", "Customer Phone",
                    "Items Count", "Original Total (INR)", "Discount (INR)", "Delivery (INR)",
                    "Tax (INR)", "Final Amount (INR)", "Payment Method", "Order Status"
            };

            org.apache.poi.ss.usermodel.Row headRow = sheet.createRow(2);
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 3;
            double sumFinal = 0.0;
            double sumOrig = 0.0;
            double sumDisc = 0.0;

            for (Order o : orders) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(o.getOrderNumber());
                row.createCell(1).setCellValue(o.getCreatedAt() != null ? DATE_FMT.format(o.getCreatedAt()) : "");
                row.createCell(2).setCellValue(o.getCustomerSnapshot() != null ? o.getCustomerSnapshot().getName() : "");
                row.createCell(3).setCellValue(o.getCustomerSnapshot() != null ? o.getCustomerSnapshot().getPhone() : "");
                row.createCell(4).setCellValue(o.getItems() != null ? o.getItems().size() : 0);
                row.createCell(5).setCellValue(o.getOriginalTotal() != null ? o.getOriginalTotal() : 0.0);
                row.createCell(6).setCellValue(o.getDiscountTotal() != null ? o.getDiscountTotal() : 0.0);
                row.createCell(7).setCellValue(o.getDeliveryCharge() != null ? o.getDeliveryCharge() : 0.0);
                row.createCell(8).setCellValue(o.getTaxTotal() != null ? o.getTaxTotal() : 0.0);
                row.createCell(9).setCellValue(o.getFinalTotal() != null ? o.getFinalTotal() : 0.0);
                row.createCell(10).setCellValue(o.getPaymentMethod() != null ? o.getPaymentMethod() : "");
                row.createCell(11).setCellValue(o.getOrderStatus() != null ? o.getOrderStatus() : "");

                sumFinal += o.getFinalTotal() != null ? o.getFinalTotal() : 0.0;
                sumOrig += o.getOriginalTotal() != null ? o.getOriginalTotal() : 0.0;
                sumDisc += o.getDiscountTotal() != null ? o.getDiscountTotal() : 0.0;
            }

            // Summary Row
            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowIdx);
            org.apache.poi.ss.usermodel.Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.setFont(totalFont);

            org.apache.poi.ss.usermodel.Cell sumLabel = totalRow.createCell(0);
            sumLabel.setCellValue("TOTALS (" + orders.size() + " orders)");
            sumLabel.setCellStyle(totalStyle);

            totalRow.createCell(5).setCellValue(sumOrig);
            totalRow.createCell(6).setCellValue(sumDisc);
            totalRow.createCell(9).setCellValue(sumFinal);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Excel sales report: ", e);
            throw new RuntimeException("Error generating Excel report: " + e.getMessage());
        }
    }

    public byte[] exportSalesPdf(LocalDate from, LocalDate to) {
        List<Order> orders = getOrdersForRange(from, to);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(document, baos);
            document.open();

            Color brandGreen = new Color(12, 131, 31);
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, brandGreen);
            com.lowagie.text.Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            com.lowagie.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            com.lowagie.text.Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);

            document.add(new Paragraph("MYMART — Sales Summary Report", titleFont));
            document.add(new Paragraph("Period: " + from + " to " + to + " | Generated on: " + DATE_FMT.format(Instant.now()), subFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14, 16, 18, 8, 11, 11, 11, 11});

            String[] headers = {"Order #", "Date", "Customer", "Items", "Orig (Rs)", "Disc (Rs)", "Final (Rs)", "Status"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, headFont));
                c.setBackgroundColor(brandGreen);
                c.setPadding(5);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(c);
            }

            double grandTotal = 0.0;
            for (Order o : orders) {
                grandTotal += o.getFinalTotal() != null ? o.getFinalTotal() : 0.0;

                table.addCell(new Phrase(o.getOrderNumber(), cellFont));
                table.addCell(new Phrase(o.getCreatedAt() != null ? DATE_FMT.format(o.getCreatedAt()) : "", cellFont));
                table.addCell(new Phrase(o.getCustomerSnapshot() != null ? o.getCustomerSnapshot().getName() : "", cellFont));
                table.addCell(new Phrase(String.valueOf(o.getItems() != null ? o.getItems().size() : 0), cellFont));
                table.addCell(new Phrase(String.format("%.2f", o.getOriginalTotal()), cellFont));
                table.addCell(new Phrase(String.format("%.2f", o.getDiscountTotal()), cellFont));
                table.addCell(new Phrase(String.format("%.2f", o.getFinalTotal()), boldCellFont));
                table.addCell(new Phrase(o.getOrderStatus(), cellFont));
            }

            document.add(table);

            document.add(new Paragraph(" "));
            Paragraph pGrand = new Paragraph("Total Orders: " + orders.size() + " | Grand Sales Revenue: Rs. " + String.format("%.2f", grandTotal),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, brandGreen));
            document.add(pGrand);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF sales report: ", e);
            throw new RuntimeException("Error generating PDF report: " + e.getMessage());
        }
    }
}
