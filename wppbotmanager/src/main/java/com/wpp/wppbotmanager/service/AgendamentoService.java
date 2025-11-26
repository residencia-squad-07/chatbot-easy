package com.wpp.wppbotmanager.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.wpp.wppbotmanager.dto.AgendProxExecDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.wpp.wppbotmanager.client.AgendBd;
import com.wpp.wppbotmanager.dto.AgendamentoDto;
import org.springframework.web.client.RestTemplate;

@Service
public class AgendamentoService {

  private final AgendBd agendBd;
  private final RestTemplate restTemplate = new RestTemplate();
  public AgendamentoService(AgendBd agendBd) {
    this.agendBd = agendBd;
  }

  public String getAgend() {
    return agendBd.getAgend();
  }

  public String postAgend(LocalDate data_solicitacao, LocalDate proxima_execucao, String status, Integer id_usuario) {
    try {
      AgendamentoDto request = new AgendamentoDto(null, data_solicitacao, proxima_execucao, status, id_usuario);
        return agendBd.createAgend(request);
    } catch(Exception e) {
      return "erro ao criar Agendamento: " + e.getMessage();
    }
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

  public String getAgendByUserId(Integer id_user) {
      return agendBd.getAgendByUserId(id_user);
  }

    public String updateProxExec(Integer id, AgendProxExecDto pexecDto) {

        LocalDate novaData = LocalDate.parse(pexecDto.getProxima_execucao());

        return agendBd.updateProxExec(id, novaData);
    }
}


