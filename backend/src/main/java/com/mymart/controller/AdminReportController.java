package com.mymart.controller;

import com.mymart.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Reports", description = "Downloadable sales and order reports in CSV, Excel XLSX, and PDF")
public class AdminReportController {

    private final ReportExportService reportExportService;

    @GetMapping("/sales")
    @Operation(summary = "Export sales report for date range (CSV, Excel XLSX, or PDF)")
    public ResponseEntity<byte[]> exportSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "csv") String format) {

        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusMonths(6); // Default 6 months as required

        String filenamePrefix = "mymart-sales-" + startDate + "-to-" + endDate;

        if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
            byte[] excelBytes = reportExportService.exportSalesExcel(startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenamePrefix + ".xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfBytes = reportExportService.exportSalesPdf(startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenamePrefix + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } else {
            // CSV Default
            byte[] csvBytes = reportExportService.exportSalesCsv(startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenamePrefix + ".csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvBytes);
        }
    }

    @GetMapping("/orders")
    @Operation(summary = "Export orders report as CSV")
    public ResponseEntity<byte[]> exportOrdersReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusMonths(6);

        byte[] csvBytes = reportExportService.exportSalesCsv(startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mymart-orders-" + startDate + "-to-" + endDate + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
