package com.wpp.wppbotmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class EnviarResumoService {
    private final MessageService messageService;
    public EnviarResumoService(MessageService messageService) {
        this.messageService = messageService;
    }
    private JsonNode unwrapMessageEnvelope(JsonNode node) {
        if (node == null || node.isNull()) return node;
        JsonNode cur = node;
        while (cur != null && cur.has("message")) {
            cur = cur.get("message");
        }
        if (cur != null && cur.isArray() && cur.size() > 0) {
            return cur.get(0);
        }
        return cur;
    }
    private final ObjectMapper objectMapper = new ObjectMapper();
    public void enviarRelatorio(String numUser, int dias,String dataInicio, String dataFim, ReceiveReportRequest reportRequest) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String idEmpresa = reportRequest.getIdEmpresa();
            if (idEmpresa == null || idEmpresa.isBlank()) {
                messageService.sendMessage(numUser, "Nenhuma empresa associada ao seu usuário.");
                return;
            }

            String empresaUrl = "http://localhost:3001/empresa/listarempresa/" + idEmpresa;
            String empresaJson = restTemplate.getForObject(empresaUrl, String.class);
            if (empresaJson == null || empresaJson.isBlank()) {
                messageService.sendMessage(numUser, "Empresa não encontrada.");
                return;
            }

            JsonNode empresaNode = objectMapper.readTree(empresaJson);
            empresaNode = unwrapMessageEnvelope(empresaNode);

            if (empresaNode == null || empresaNode.isNull()) {
                messageService.sendMessage(numUser, "Erro ao interpretar dados da empresa.");
                return;
            }

            if (empresaNode.isArray() && empresaNode.size() > 0) {
                empresaNode = empresaNode.get(0);
            }

            String appKey = empresaNode.path("app_key").asText(empresaNode.path("appKey").asText(null));
            String appSecret = empresaNode.path("app_secret").asText(empresaNode.path("appSecret").asText(null));

            if (appKey == null || appSecret == null) {
                messageService.sendMessage(numUser, "Dados de integração não encontrados.");
                return;
            }

            String url;
            if (dias != 0) {
                url = "http://localhost:3001/omie/relatorio-financeiro?dias=" + dias;
            } else {
                url = "http://localhost:3001/omie/relatorio-financeiro?data_inicio=" + dataInicio + "&data_fim=" + dataFim;
            }

            Map<String, Object> body = new HashMap<>();
            body.put("appKey", appKey);
            body.put("appSecret", appSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(url, entity, String.class);
            if (response == null) {
                messageService.sendMessage(numUser, "Erro ao gerar relatório.");
                return;
            }
            try {
                JsonNode jsonResponse = objectMapper.readTree(response);
                JsonNode resumo = jsonResponse.path("resumo_geral");
                JsonNode periodo = resumo.path("periodo_analisado");
                int totalDias = periodo.path("total_dias").asInt(0);
                String totalReceitas = resumo.path("total_receitas").asText("");
                String totalDespesas = resumo.path("total_despesas_custos").asText("");
                String resultado = resumo.path("resultado_liquido").asText("");
                JsonNode cat = jsonResponse.path("detalhes_por_categoria");
                String out = "📊 *Resumo Geral – Período Analisado*\n" +
                        "🗓️ De: " + dataInicio + "\n" +
                        "🗓️ Até: " + dataFim + "\n\n" +
                        "📅 *Duração:* " + totalDias + " dias\n\n" +
                        "💰 *Totais*\n\n" +
                        "Receitas: " + totalReceitas + "\n\n" +
                        "Despesas / Custos: " + totalDespesas + "\n\n" +
                        "Resultado Líquido: " + resultado + "\n\n" +
                        "📂 *Detalhamento por Categoria*\n\n" +
                        "Receitas\n\n" +
                        "Receitas Operacionais: " + cat.path("receitas_operacionais").asText("") + "\n" +
                        "Entradas Não Operacionais: " + cat.path("entradas_nao_operacionais").asText("") + "\n\n" +
                        "Despesas e Custos\n\n" +
                        "Custos Variáveis: " + cat.path("custos_variaveis").asText("") + "\n" +
                        "Despesas com Pessoal: " + cat.path("despesas_com_pessoal").asText("") + "\n" +
                        "Despesas Administrativas: " + cat.path("despesas_administrativas").asText("") + "\n" +
                        "Pró-labore: " + cat.path("pro_labore").asText("") + "\n" +
                        "Investimentos: " + cat.path("investimentos").asText("") + "\n" +
                        "Parcelamentos: " + cat.path("parcelamentos").asText("") + "\n" +
                        "Saídas Não Operacionais: " + cat.path("saidas_nao_operacionais").asText("") + "\n";
                messageService.sendMessage(numUser, out);

            } catch (Exception e) {
                messageService.sendMessage(numUser, response);
            }

        } catch (Exception e) {
            messageService.sendMessage(numUser, "Erro interno: " + e.getMessage());
        }
    }
}
