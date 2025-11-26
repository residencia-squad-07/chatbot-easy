package com.wpp.wppbotmanager.controller;

import java.time.LocalDate;

import com.wpp.wppbotmanager.dto.AgendProxExecDto;
import com.wpp.wppbotmanager.dto.AgendamentoDto;
import org.springframework.web.bind.annotation.*;

import com.wpp.wppbotmanager.service.AgendamentoService;

@RestController
@RequestMapping("/agend")
public class AgendamentoController {
  
  private final AgendamentoService agendService;

  public AgendamentoController(AgendamentoService agendService) {
    this.agendService = agendService;
  }

  @GetMapping("/lagend")
  public String getAgend() {
    return agendService.getAgend();
  }

  @PostMapping("/cagend")
  public String postAgend(@RequestBody LocalDate data_solicitacao, @RequestBody LocalDate proxima_execucao, @RequestBody String status, @RequestBody Integer id_usuario){
      return agendService.postAgend(data_solicitacao, proxima_execucao, status, id_usuario);
    }

  @GetMapping("/gagendui/:id_user")
  public String getAgendByUserId(Integer id_user) {
      return agendService.getAgendByUserId(id_user);
  }

  @PutMapping("/pproxecec/:id")
  public String updateProxExec(@PathVariable Integer id,@RequestBody AgendProxExecDto pexecDto) {
      return agendService.updateProxExec(id, pexecDto);
  }
}
