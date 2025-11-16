package com.wpp.wppbotmanager.service;

import org.springframework.stereotype.Service;

import com.wpp.wppbotmanager.client.UserBd;
import com.wpp.wppbotmanager.dto.UserDto;

@Service
public class UserService {

  private final UserBd userBd;

  public UserService(UserBd userBd) {
    this.userBd = userBd;
  }

  public String getUser() {
    return userBd.getUser();
  }
  
  public String createUser(UserDto userDto) {
    return userBd.createUser(userDto);
  }

  public String updateUser(Integer id, UserDto userDto) {
      return userBd.updateUser(id, userDto);
  }

  public String marcarPMensagem(String telefone, UserDto userDto) {
      return userBd.marcarPMensagem(telefone, userDto);
  }

  public String deleteUser(Integer id) {
      return userBd.deleteUser(id);
  }

  public String getUserById(Integer id) {
      return userBd.getUserById(id);
  }
}
