package com.wpp.wppbotmanager.controller;

import com.wpp.wppbotmanager.dto.OmieDTO;
import com.wpp.wppbotmanager.service.PDFGenerationService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PDFExportController {

    @Autowired
    private PDFGenerationService pdfService;

    @PostMapping("/export/relatorio-financeiro")
    public ResponseEntity<byte[]> exportPdf(@RequestBody OmieDTO.OmieApiRequest request) {
        try {
            byte[] pdfBytes = pdfService.gerarRelatorioFinanceiroPdf(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "relatorio_financeiro.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (JRException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(502).body(null);
        }
    }
}
