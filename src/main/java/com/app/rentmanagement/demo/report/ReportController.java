package com.app.rentmanagement.demo.report;


import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ExcelReportService excelReportService;
    private final PdfReportService pdfReportService;

    @GetMapping("/monthly-collection/excel")
    public ResponseEntity<byte[]> downloadMonthlyCollectionExcel() {

        byte[] data = excelReportService.generateMonthlyCollectionReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=monthly-collection.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/settlement/pdf")
    public ResponseEntity<byte[]> downloadSettlementPdf() {

        byte[] data = pdfReportService.generateSettlementReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=settlement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}