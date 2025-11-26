package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.client.AgendBd;
import com.wpp.wppbotmanager.client.UserBd;
import com.wpp.wppbotmanager.dto.AgendamentoDto;
import com.wpp.wppbotmanager.dto.UserDto;
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
    private final MessageService messageService;
    private final ChatbotService chatbotService;
    private final MessageServiceAuto messageServiceAuto;
    private final UserBd userBd;

    public AgendAutoService(AgendBd agendBd, MessageService messageService, ChatbotService chatbotService, MessageServiceAuto messageServiceAuto, UserBd userBd) {
        this.agendBd = agendBd;
        this.messageService = messageService;
        this.chatbotService = chatbotService;
        this.messageServiceAuto = messageServiceAuto;
        this.userBd = userBd;
    }

    @Scheduled(cron = "* * 3 * * *")
    public void enviarAgendamento() {
        System.out.println("[AGENDAMENTO] Enviando agendamento...");

        AgendamentoDto[] agendamentos = agendBd.getAgendAsDto();

        if (agendamentos == null || agendamentos.length == 0) {
            System.out.println("[SCHEDULE] Nenhum agendamento encontrado.");
            return;
        }

        LocalDate hoje = LocalDate.now();

        for (AgendamentoDto ag : agendamentos) {

            LocalDate prox = ag.getProxima_execucao();

            // Caso seja NULL → ajusta para hoje e já segue
            if (prox == null) {
                System.out.println("[SCHEDULE] proxima_execucao NULL. Atualizando para hoje...");

                LocalDate novaExecucao = hoje.plusWeeks(2);

                // Atualiza no banco
                String respostaNode = agendBd.updateProxExec(
                        ag.getId_usuario(),
                        novaExecucao
                );

                System.out.println("[SCHEDULE] proxima_execucao inicial definida como: " + novaExecucao);

                // Executa ação agora
                executarAcaoSemanal(ag.getId_usuario());

                continue; // passa para o próximo
            }

            // Se já está vencida (igual ou menor que hoje)
            if (!prox.isAfter(hoje)) {

                System.out.println("[SCHEDULE] Executando agendamento ID: " + ag.getId_agendamento() +
                        " | Usuário: " + ag.getId_usuario());

                // Executar ação do usuário
                executarAcaoSemanal(ag.getId_usuario());

                // Gerar nova data
                LocalDate novaExecucao = hoje.plusWeeks(2);

                String respostaNode = agendBd.updateProxExec(
                        ag.getId_usuario(),
                        novaExecucao
                );

                System.out.println("[SCHEDULE] Próxima execução atualizada para: " + novaExecucao);
                System.out.println("[SCHEDULE] Resposta Node: " + respostaNode);
            }
        }

        System.out.println("[SCHEDULE] Verificação concluída.");
    }


    private void executarAcaoSemanal(Integer idUsuario) {
        System.out.println("[AÇÃO SEMANAL] Enviando mensagem para usuário " + idUsuario);

        UserDto user = userBd.getUserDtoById(idUsuario);

        if (user == null || user.getTelefone() == null) {
            System.out.println("[AÇÃO SEMANAL] Usuário sem telefone cadastrado.");
            return;
        }

        String numero = user.getTelefone();
        String texto = "ENVIADO PDF DE 15 DIAS!";
        messageService.sendMessage(numero, texto);
        // chatbotService.sendAutomaticMessage(idUsuario);
        // messageService.sendMessage(numero, texto);
    }
}
