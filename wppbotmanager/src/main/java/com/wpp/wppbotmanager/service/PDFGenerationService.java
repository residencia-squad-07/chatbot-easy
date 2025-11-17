package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.dto.OmieDTO;
import com.wpp.wppbotmanager.model.RelatorioFinanceiroModel;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class PDFGenerationService {

    private final WebClient webClient;

    public PDFGenerationService(WebClient webClient) {
        this.webClient = webClient;
    }

    public byte[] gerarRelatorioFinanceiroPdf(OmieDTO.OmieApiRequest request) throws JRException {
        RelatorioFinanceiroModel dadosFinanceiros = buscarDadosFinanceiros(request);

        InputStream jasperStream = this.getClass().getResourceAsStream("/reports/relatorioestruturado.jasper");
        InputStream imageStream = getClass().getResourceAsStream("/images/logo.png");
        Integer diasAnalisados = request.getDias();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("resumoGeral", dadosFinanceiros.getResumoGeral());
        parameters.put("detalhesPorCategoria", dadosFinanceiros.getDetalhesPorCategoria());
        parameters.put("Logo_Imagem", imageStream);
        parameters.put("DIAS_ANALISADOS", diasAnalisados);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(dadosFinanceiros));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private RelatorioFinanceiroModel buscarDadosFinanceiros(OmieDTO.OmieApiRequest request) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/omie/relatorio-financeiro")
                        .queryParam("dias", request.getDias())
                        .build())
                .bodyValue(request) // Envia o objeto inteiro no corpo
                .retrieve()
                .bodyToMono(RelatorioFinanceiroModel.class)
                .block();
    }
}
