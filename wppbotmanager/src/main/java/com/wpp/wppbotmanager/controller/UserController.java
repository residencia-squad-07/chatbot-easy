package com.wpp.wppbotmanager.controller;

import org.springframework.web.bind.annotation.*;
import com.wpp.wppbotmanager.dto.UserDto;
import com.wpp.wppbotmanager.service.UserService;


@RestController
@RequestMapping("/usuario")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/luser")
    public String getUser() {
        return userService.getUser();
    }

    @PostMapping("/cuser")
    public String createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/uuser/{id}")
    public String updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        return userService.updateUser(id, userDto);
    }

    @PutMapping("/pmensagem/{telefone}")
    public String marcarPMensagem(@PathVariable String telefone, @RequestBody UserDto userDto) {
        return userService.marcarPMensagem(telefone, userDto);
    }

    @DeleteMapping("/duser/{id}")
    public String deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id);
    }

    @GetMapping("/guser/{id}")
    public String getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }
    @GetMapping("/euser/{id_empresa}")
    public String getUsuariosByEmpresa(@PathVariable Integer id_empresa) {
        return userService.getUsuariosByEmpresa(id_empresa);
    }
}