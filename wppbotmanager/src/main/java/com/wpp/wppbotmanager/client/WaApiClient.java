package com.wpp.wppbotmanager.client;

import com.wpp.wppbotmanager.dto.SendMessageRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WaApiClient {

    private final WebClient webClient;

  public WaApiClient(WebClient.Builder builder) {
    this.webClient = builder
      .baseUrl("http://localhost:3001/wpp")
      .build();
  }

  @Getter
  @AllArgsConstructor
  private static class SendDocumentRequest {
      private String numero;
      private String documento; // Base64
      private String nomeArquivo;
  }

  public String getMessages() {
    return webClient.get()
      .uri("/listar")
      .retrieve()
      .bodyToMono(String.class)
      .doOnError(e -> System.err.println("Erro ao chamar API TS: " + e.getMessage()))
      .block();
  }

  public String sendMessage(SendMessageRequest request) {
    return webClient.post()
      .uri("/enviar")
      .header("Content-Type", "application/json")
      .bodyValue(request)
      .retrieve()
      .bodyToMono(String.class)
      .doOnError(e -> System.err.println("Erro ao enviar a mensagem: " + e.getMessage()))
      .block();
  }

  public String sendDocument(String numero, String documentoBase64, String nomeArquivo) {
      SendDocumentRequest request = new SendDocumentRequest(numero, documentoBase64, nomeArquivo);
      return webClient.post()
              .uri("/enviar-documento") // CORRIGIDO
              .header("Content-Type", "application/json")
              .bodyValue(request)
              .retrieve()
              .bodyToMono(String.class)
              .doOnError(e -> System.err.println("Erro ao enviar o documento: " + e.getMessage()))
              .block();
  }
}
