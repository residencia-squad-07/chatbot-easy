package com.wpp.wppbotmanager.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.wpp.wppbotmanager.dto.userAdmDto;

@Component
public class userAdmBd {

    private final WebClient userAdmTb;

    public userAdmBd(WebClient.Builder builder) {
        this.userAdmTb = builder
                .baseUrl("http://localhost:3001/useradmin")
                .build();
    }

    public String getUserAdm() {
        return userAdmTb.get()
                .uri("/luseradm")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String postUserAdm(userAdmDto request) {
        return userAdmTb.post()
                .uri("/cuseradm")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
