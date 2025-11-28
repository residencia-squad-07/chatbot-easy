package com.wpp.wppbotmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class OmieDTO {

    @Getter
    @Setter
    public static class OmieApiRequest {
        private String appKey;
        private String appSecret;

        @JsonProperty("data_inicio")
        private String dataInicio;

        @JsonProperty("data_fim")
        private String dataFim;

        // O construtor agora não é mais necessário, o Jackson cuidará da criação.
        // Se você precisar criar este objeto manualmente em algum lugar, adicione um construtor vazio.
        public OmieApiRequest() {}
    }
}
