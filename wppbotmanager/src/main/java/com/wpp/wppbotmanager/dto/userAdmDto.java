package com.wpp.wppbotmanager.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class userAdmDto {
    private int id_usuario;
    private String senha;
    private String nome;
    private String email;

    public userAdmDto(Integer id_usuario, String senha, String nome, String email) {
        this.id_usuario = id_usuario;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
    }
}
