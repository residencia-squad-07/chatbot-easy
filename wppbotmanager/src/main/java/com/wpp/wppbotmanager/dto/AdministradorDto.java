package com.wpp.wppbotmanager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdministradorDto {

  private Integer id_admin;
  private String nome;
  private String email;
  private String senha;


  public AdministradorDto(Integer id_admin, String nome, String email, String senha) {
    this.id_admin = id_admin;
    this.nome = nome;
    this.email = email;
    this.senha = senha;
  }
}
