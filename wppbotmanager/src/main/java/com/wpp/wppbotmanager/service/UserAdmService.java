package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.client.userAdmBd;

import com.wpp.wppbotmanager.dto.userAdmDto;
import org.springframework.stereotype.Service;

@Service
public class UserAdmService {
    private final userAdmBd userAdmBd;
    public UserAdmService(userAdmBd userAdmBd) {
        this.userAdmBd = userAdmBd;
    }

    public String getUserAdm() {
        return userAdmBd.getUserAdm();
    }
    public String postUserAdm( String senha, String nome, String email) {
        try {
            userAdmDto request = new userAdmDto (null, senha, nome, email);
            return userAdmBd.postUserAdm(request);
        } catch(Exception e) {
            return "erro ao criar Usuario Administrador: " + e.getMessage();
        }
    }
}
