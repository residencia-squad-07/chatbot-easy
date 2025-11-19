package com.wpp.wppbotmanager.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.wpp.wppbotmanager.client.AgendBd;
import com.wpp.wppbotmanager.dto.AgendamentoDto;

@Service
public class AgendamentoService {

  private final AgendBd agendBd;

  public AgendamentoService(AgendBd agendBd) {
    this.agendBd = agendBd;
  }

  public String getAgend() {
    return agendBd.getAgend();
  }

  public String postAgend(LocalDate data_solicitacao, LocalDate proxima_execucao, Integer status, Integer id_usuario) {
    try {
      AgendamentoDto request = new AgendamentoDto(null, data_solicitacao, proxima_execucao, status, id_usuario);
        return agendBd.createAgend(request);
    } catch(Exception e) {
      return "erro ao criar Agendamento: " + e.getMessage();
    }
  }

  public String getAgendByUserId(Integer id_user) {
      return agendBd.getAgendByUserId(id_user);
  }
}
