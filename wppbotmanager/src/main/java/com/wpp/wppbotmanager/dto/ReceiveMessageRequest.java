package com.wpp.wppbotmanager.dto;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveMessageRequest {
    private String id;
    private String from;
    private String nome;
    private String status;
    private String texto;
    private String papel;
    private String data;
    private String hora;
    private String id_empresa;
    private String primeiro_contato;
}
