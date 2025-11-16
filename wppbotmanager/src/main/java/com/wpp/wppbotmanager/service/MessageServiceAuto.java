package com.wpp.wppbotmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.wpp.wppbotmanager.dto.ReceiveMessageRequest;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MessageServiceAuto {

    private final MessageService messageService;
    private final ChatbotService chatbotService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> mensagensProcessadas = new HashSet<>();

    public MessageServiceAuto(MessageService messageService, ChatbotService chatbotService) {
        this.messageService = messageService;
        this.chatbotService = chatbotService;
    }

    /**
     * Método reaproveitável para processar uma mensagem individual
     */
    public void processarMensagemIndividual(ReceiveMessageRequest msg) {
        String numUser = msg.getFrom();
        System.out.println("[DEBUG] Nova mensagem recebida manualmente de: " + numUser);

        try {
            String url = "http://localhost:3001/users/telefone/" + numUser;
            RestTemplate restTemplate = new RestTemplate();
            String userJson = restTemplate.getForObject(url, String.class);

            System.out.println("[DEBUG] JSON recebido do Node: " + userJson);

            if (userJson == null || userJson.isBlank()) {
                System.out.println("[DEBUG] Usuário não encontrado no banco: " + numUser);
                chatbotService.unknownUser(numUser);
                return;
            }

            JsonNode node = mapper.readTree(userJson);
            System.out.println("[DEBUG] JSON parseado: " + node.toPrettyString());

            if (node.has("message")) {
                node = node.get("message");
            }

            // Extração de dados
            String idEmpresa = node.path("id_empresa").asText(null);
            String atividade = node.path("atividade").asText("ativo").toLowerCase();
            String papel = node.path("papel").asText("user");
            String nome = node.path("nome").asText(null);
            String primeiroContato = node.path("primeiro_contato").asText("nao");

            System.out.println("[DEBUG] id_empresa: " + idEmpresa);
            System.out.println("[DEBUG] atividade: " + atividade);
            System.out.println("[DEBUG] papel: " + papel);
            System.out.println("[DEBUG] nome: " + nome);
            System.out.println("[DEBUG] primeiro_contato: " + primeiroContato);

            // Preenche DTO
            msg.setId_empresa(idEmpresa);
            msg.setStatus(atividade);
            msg.setPapel(papel);
            msg.setNome(nome);
            msg.setPrimeiro_contato(primeiroContato);

            // Fluxo
            if ("ativo".equalsIgnoreCase(atividade)) {
                ReceiveReportRequest report = new ReceiveReportRequest();
                report.setIdEmpresa(idEmpresa);
                chatbotService.processMessage(msg, report);

            } else if ("inativo".equalsIgnoreCase(atividade)) {
                chatbotService.inactiveUser(numUser);

            } else {
                chatbotService.unknownUser(numUser);
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Erro ao processar usuário: " + e.getMessage());
            e.printStackTrace();
            chatbotService.unknownUser(numUser);
        }
    }

    /**
     * Execução automática a cada segundo
     */
    @Scheduled(fixedRate = 1000)
    public void processarMensagensAutomaticamente() {
        try {
            String json = messageService.getMessageApi();

            List<ReceiveMessageRequest> mensagens = mapper.readValue(
                    json, new TypeReference<>() {}
            );

            for (ReceiveMessageRequest msg : mensagens) {

                if (mensagensProcessadas.contains(msg.getId())) {
                    continue;
                }

                processarMensagemIndividual(msg);

                mensagensProcessadas.add(msg.getId());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Erro no agendamento: " + e.getMessage());
        }
    }
}
