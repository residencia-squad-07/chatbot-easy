package com.wpp.wppbotmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wpp.wppbotmanager.dto.enums.atividade.Atividade;
import com.wpp.wppbotmanager.dto.enums.first_contact;
import com.wpp.wppbotmanager.dto.enums.papel.Papel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL) // NÃO serializa campos com valor null
public class UserDto {

    private String nome;
    private String telefone;
    private Papel papel;
    private Atividade atividade;
    private Integer id_empresa;
    private first_contact.First_Contact first_contact;

    public UserDto() {}

    public UserDto(String nome, String telefone, Papel papel, Integer id_empresa, first_contact.First_Contact first_contact) {
        this.nome = nome;
        this.telefone = telefone;
        this.papel = papel;
        this.id_empresa = id_empresa;
        this.first_contact = first_contact;
    }
}