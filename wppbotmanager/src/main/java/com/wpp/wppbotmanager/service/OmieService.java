package com.wpp.wppbotmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpp.wppbotmanager.client.EmpresaBd;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class OmieService {

    @Autowired
    private EmpresaBd empresaBd;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void gerarRelatorioFinanceiroGeral(int dias, ReceiveReportRequest request) {
        try {
            String respostaJson = empresaBd.getEmpresa();
            JsonNode empresas = objectMapper.readTree(respostaJson);

            JsonNode empresaSelecionada = null;
            for (JsonNode empresa : empresas) {
                if (empresa.get("id").asText().equals(request.getIdEmpresa())) {
                    empresaSelecionada = empresa;
                    break;
                }
            }

            if (empresaSelecionada == null) {
                throw new RuntimeException("Empresa não encontrada com ID " + request.getIdEmpresa());
            }

            String appKey = empresaSelecionada.path("app_Key").asText();
            String appSecret = empresaSelecionada.path("app_Secret").asText();
            String nomeEmpresa = empresaSelecionada.path("nome").asText();

            System.out.println("Gerando relatório de " + dias + " dias para empresa " + nomeEmpresa);

            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:3001/omie/gerar-relatorio";

            Map<String, Object> body = new HashMap<>();
            body.put("app_Key", appKey);
            body.put("app_Secret", appSecret);
            body.put("dias", dias);
            body.put("empresa_nome", nomeEmpresa);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            System.out.println("Resposta da API Node: " + response.getBody());

        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }
}
