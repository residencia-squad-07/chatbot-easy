package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.client.AgendBd;
import com.wpp.wppbotmanager.dto.AgendamentoDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AgendAutoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AgendBd agendBd;

    public AgendAutoService(AgendBd agendBd) {
        this.agendBd = agendBd;
    }

    public void criarAgendamento(Integer idUser) {
        System.out.println("[AGENDAMENTO] Criando agendamento pro user id: " + idUser);

        try {
            String url = "http://localhost:3001/agend/cagend";

            Map<String, Object> body = new HashMap<>();
            body.put("id_usuario", idUser);
            body.put("data_solicitacao", LocalDate.now().toString());
            body.put("status", "ativo");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String resposta = restTemplate.postForObject(url, request, String.class);

            System.out.println("[DEBUG] Agendamento criado com sucesso! Resposta Node: " + resposta);
            System.out.println("[DEBUG] Agendamento criado com sucesso!");

        } catch (Exception e) {
            System.out.println("[AGENDAMENTO] Erro ao criar agendamento: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 3 * * *") // as 3 da manha
    public void enviarAgendamento() {
        System.out.println("[AGENDAMENTO] Enviando agendamento...");

        AgendamentoDto[] agendamentos = agendBd.getAgendAsDto();

        if (agendamentos == null || agendamentos.length == 0) {
            System.out.println("[SCHEDULE] Nenhum agendamento encontrado.");
            return;
        }
        LocalDate hoje = LocalDate.now();

        for (AgendamentoDto ag : agendamentos) {
            if (ag.getProxima_execucao() == null) continue;

            // Verifica se HOJE é a data da execução
            if (ag.getProxima_execucao().isEqual(hoje)) {

                System.out.println("[SCHEDULE] Executando agendamento ID: " + ag.getId_agendamento() +
                        " | Usuário: " + ag.getId_usuario());

                // 1. EXECUTAR FUNÇÃO
                executarAcaoSemanal(ag.getId_usuario());

                // 2. GERAR nova data
                LocalDate novaExecucao = hoje.plusWeeks(1);

                // 3. Atualizar
                AgendamentoDto updateDto = new AgendamentoDto(
                        ag.getId_agendamento(),
                        ag.getData_solicitacao(),
                        novaExecucao,
                        ag.getStatus(),
                        ag.getId_usuario()
                );

                String respostaNode = agendBd.updateAgend(ag.getId_agendamento(), updateDto);

                System.out.println("[SCHEDULE] Próxima execução atualizada para: " + novaExecucao);
                System.out.println("[SCHEDULE] Resposta Node: " + respostaNode);
            }

        }
        System.out.println("[SCHEDULE] Verificação concluída.");

    }

    private void executarAcaoSemanal(Integer idUsuario) {
        System.out.println("[AÇÃO SEMANAL] Enviando mensagem para usuário " + idUsuario);

        // chatbotService.sendAutomaticMessage(idUsuario);
        // messageService.sendMessage(numero, texto);
    }
}
