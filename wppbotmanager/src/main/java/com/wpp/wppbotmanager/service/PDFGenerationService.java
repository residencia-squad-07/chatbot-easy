package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.dto.OmieDTO;
import com.wpp.wppbotmanager.model.RelatorioFinanceiroModel;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class PDFGenerationService {

    private final WebClient webClient;

    public PDFGenerationService(WebClient webClient) {
        this.webClient = webClient;
    }

    public byte[] gerarRelatorioFinanceiroPdf(OmieDTO.OmieApiRequest request) throws JRException, IOException {
        RelatorioFinanceiroModel dadosFinanceiros = buscarDadosFinanceiros(request);

        Resource caminho = new ClassPathResource("reports/relatorioestruturado.jrxml");
        InputStream jasperStream = caminho.getInputStream();


        Resource image = new ClassPathResource("logo.png");
        InputStream imagePath = image.getInputStream();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate dataInicio = LocalDate.parse(dadosFinanceiros.getResumoGeral().getPeriodoAnalisado().getData_inicio(), formatter);
        LocalDate dataFim = LocalDate.parse(dadosFinanceiros.getResumoGeral().getPeriodoAnalisado().getData_fim(), formatter);
        long diasAnalisados = ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("resumoGeral", dadosFinanceiros.getResumoGeral());
        parameters.put("detalhesPorCategoria", dadosFinanceiros.getDetalhesPorCategoria());
        parameters.put("Logo_Imagem", imagePath);
        parameters.put("DIAS_ANALISADOS", (int) diasAnalisados);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(dadosFinanceiros));

        JasperReport jasperReport = JasperCompileManager.compileReport(jasperStream);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private RelatorioFinanceiroModel buscarDadosFinanceiros(OmieDTO.OmieApiRequest request) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/omie/relatorio-financeiro")
                        .queryParam("data_inicio", request.getDataInicio())
                        .queryParam("data_fim", request.getDataFim())
                        .build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RelatorioFinanceiroModel.class)
                .block();
    }
}