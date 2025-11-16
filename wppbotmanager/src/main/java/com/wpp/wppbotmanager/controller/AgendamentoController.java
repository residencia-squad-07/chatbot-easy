package com.wpp.wppbotmanager.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wpp.wppbotmanager.service.AgendamentoService;

@RestController
@RequestMapping("/agend")
public class AgendamentoController {
  
  private final AgendamentoService agendService;

  public AgendamentoController(AgendamentoService agendService) {
    this.agendService = agendService;
  }

  @GetMapping("/lagend")
  public String getAdmin() {
    return agendService.getAgend();
  }

  @PostMapping("/cagend")
    public String postAdmin(@RequestBody LocalDate data_solicitacao, @RequestBody LocalDate proxima_execucao, @RequestBody Integer status, @RequestBody Integer id_usuario){
        return agendService.postAgend(data_solicitacao, proxima_execucao, status, id_usuario);
    }
}
