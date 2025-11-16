package com.wpp.wppbotmanager.controller;

import com.wpp.wppbotmanager.service.ChatbotService;
import com.wpp.wppbotmanager.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.wpp.wppbotmanager.dto.ReceiveMessageRequest;
import com.wpp.wppbotmanager.dto.SendMessageRequest;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;

import com.wpp.wppbotmanager.service.MessageServiceAuto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/wpp/messages")
public class MessageController {

  private final MessageService messageService;
  private final ChatbotService chatbotService;
    private final MessageServiceAuto messageServiceAuto;

  public MessageController(MessageService messageService, ChatbotService chatbotService, MessageServiceAuto messageServiceAuto) {
    this.messageService = messageService;
    this.chatbotService = chatbotService;
    this.messageServiceAuto = messageServiceAuto;
  }

  @GetMapping
  public String getMessages() {
    return messageService.getMessageApi();
  }

  @PostMapping("/enviar")
  public String SendMessage(@RequestBody SendMessageRequest request) {
    return messageService.sendMessage(request.getNumero(), request.getTexto());
  }

  @PostMapping("/receber/msg")
  public ResponseEntity<?> receiveMessage(@RequestBody ReceiveMessageRequest request) {
      System.out.println("[DEBUG] Incoming webhook from: " + request.getFrom());

      messageServiceAuto.processarMensagemIndividual(request);

      return ResponseEntity.ok("Mensagem recebida");
  }
}