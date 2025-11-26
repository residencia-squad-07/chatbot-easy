package com.wpp.wppbotmanager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgendProxExecDto {
    private String proxima_execucao;

    public AgendProxExecDto () {}

    public AgendProxExecDto (String proxima_execucao) {
        this.proxima_execucao = proxima_execucao;
    }
}
