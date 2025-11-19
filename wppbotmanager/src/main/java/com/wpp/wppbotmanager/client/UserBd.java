package com.wpp.wppbotmanager.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.wpp.wppbotmanager.dto.UserDto;
import reactor.core.publisher.Mono;

@Component
public class UserBd {

  private final WebClient userTb;

  public UserBd(WebClient.Builder builder) {
    this.userTb = builder
        .baseUrl("http://localhost:3001/users")
        .build();
  }

  public String getUser() {
    return userTb.get()
      .uri("/luser")
      .retrieve()
      .bodyToMono(String.class)
      .doOnError(e -> System.err.println("Erro ao chamar API TS: " + e.getMessage()))
      .block();
  }
  
  public String createUser(UserDto userDto) {
    return userTb.post()
        .uri("/cuser")
        .bodyValue(userDto)
        .exchangeToMono(response -> {
            if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(String.class);
            } else {
                return response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> {
                        String errorMsg = "Erro ao criar usuário: status=" + response.statusCode() + 
                                         " | body=" + body;
                        System.err.println(errorMsg);
                        return Mono.error(new RuntimeException(errorMsg));
                    });
            }
        })
        .block();
  }

  public String updateUser(Integer id, UserDto userDto) {
      return userTb.put()
          .uri("/uuser/{id}", id)
          .bodyValue(userDto)
          .retrieve()
          .bodyToMono(String.class)
          .doOnError(e -> System.err.println("Erro ao atualizar usuário: " + e.getMessage()))
          .block();
  }

    public String marcarPMensagem(String telefone, UserDto userDto) {
      return userTb.put()
          .uri("/pmensagem/{telefone}", telefone)
          .bodyValue(userDto)
          .retrieve()
          .bodyToMono(String.class)
          .doOnError(e -> System.err.println("Erro ao atualizar usuário: " + e.getMessage()))
          .block();
  }

  public String deleteUser(Integer id) {
      return userTb.delete()
          .uri("/duser/{id}", id)
          .retrieve()
          .bodyToMono(String.class)
          .doOnError(e -> System.err.println("Erro ao deletar usuário: " + e.getMessage()))
          .block();
  }

  public String getUserById(Integer id) {
      return userTb.get()
          .uri("/guser/{id}", id)
          .retrieve()
          .bodyToMono(String.class)
          .doOnError(e -> System.err.println("Erro ao buscar usuário por id: " + e.getMessage()))
          .block();
  }
  public String getUsuariosByEmpresa(Integer idEmpresa) {
    return userTb.get()
        .uri("/euser/{id_empresa}", idEmpresa)
        .retrieve()
        .bodyToMono(String.class)
        .doOnError(e -> System.err.println("Erro ao buscar usuários da empresa: " + e.getMessage()))
        .block();
}
public String marcarUsuarioInativo(Integer id) {
    return userTb.put()
        .uri("/marcarinativo/{id}", id)
        .retrieve()
        .bodyToMono(String.class)
        .doOnError(e -> System.err.println("Erro ao marcar usuário como inativo: " + e.getMessage()))
        .block();
}
}