package com.wpp.wppbotmanager.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgendamentoDto {
  
  private Integer id_agendamento;
  private LocalDate data_solicitacao;
  private LocalDate proxima_execucao;
  private String status;
  private Integer id_usuario;

  public AgendamentoDto () {}

  public AgendamentoDto (Integer id_agendamento, LocalDate data_solicitacao, LocalDate proxima_execucao, String status, Integer id_usuario) {
    this.id_agendamento = id_agendamento;
    this.data_solicitacao = data_solicitacao;
    this.proxima_execucao = proxima_execucao;
    this.status = status;
    this.id_usuario = id_usuario;
  }
}