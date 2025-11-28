package com.wpp.wppbotmanager.controller;

import org.apache.catalina.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wpp.wppbotmanager.service.UserAdmService;
@RestController
@RequestMapping("/useradmin")
public class userAdmController {
    private final UserAdmService UserAdmService;

    public userAdmController(UserAdmService userAdmService) {
        this.UserAdmService = userAdmService;
    }

    @GetMapping("/luseradm")
    public String getUserAdm() {
        return  UserAdmService.getUserAdm();
    }
    @PostMapping("/cuseradm")
    public String postUserAdm(@RequestBody String senha, @RequestBody String nome, @RequestBody String email){
        return UserAdmService.postUserAdm(senha, nome, email);
    }
}
