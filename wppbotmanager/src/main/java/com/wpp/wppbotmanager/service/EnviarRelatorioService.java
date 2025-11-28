package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.dto.OmieDTO;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnviarRelatorioService {

    private final PDFGenerationService pdfGenerationService;
    private final MessageService messageService;

    @Autowired
    public EnviarRelatorioService(PDFGenerationService pdfGenerationService, MessageService messageService) {
        this.pdfGenerationService = pdfGenerationService;
        this.messageService = messageService;
    }

    public void enviarRelatorioGerado(String numUser, String dataInicioFormatada, String dataFimFormatada, ReceiveReportRequest reportRequest) {
        try {
            String appKey = "5614700718627";
            String appSecret = "2ae8328ce879960d99ba83e7986805a3";

            OmieDTO.OmieApiRequest request = new OmieDTO.OmieApiRequest();
            request.setAppKey(appKey);
            request.setAppSecret(appSecret);
            request.setDataInicio(dataInicioFormatada);
            request.setDataFim(dataFimFormatada);

            byte[] pdfBytes = pdfGenerationService.gerarRelatorioFinanceiroPdf(request);

            if (pdfBytes != null && pdfBytes.length > 0) {
                System.out.println("PDF gerado, preparando para enviar...");

                String dataInicioNomeArquivo = dataInicioFormatada.replace("/", "-");
                String dataFimNomeArquivo = dataFimFormatada.replace("/", "-");
                String nomeArquivo = "Relatorio_" + dataInicioNomeArquivo + "_a_" + dataFimNomeArquivo + ".pdf";

                messageService.sendDocument(numUser, pdfBytes, nomeArquivo);

            } else {
                messageService.sendMessage(numUser, "Não foi possível gerar o relatório.");
            }
        } catch (Exception e) {
            System.err.println("Não foi possivel montar o relatório por causa de: " + e.getMessage());
            e.printStackTrace();
            messageService.sendMessage(numUser,"Ocorreu um erro interno ao gerar o relatório.");
        }
    }
}
