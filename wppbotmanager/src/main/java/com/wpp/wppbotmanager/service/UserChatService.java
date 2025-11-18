package com.wpp.wppbotmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpp.wppbotmanager.client.UserBd;
import com.wpp.wppbotmanager.dto.UserDto;
import com.wpp.wppbotmanager.dto.enums.atividade.Atividade;
import com.wpp.wppbotmanager.dto.enums.papel.Papel;
import org.springframework.stereotype.Service;
@Service
public class UserChatService {

    private final UserBd userBD;
    private final MessageService messageService;
    private final ObjectMapper mapper = new ObjectMapper();

    private String etapaCriacao = "";
    private UserDto usuarioTemp = null;


    public UserChatService(UserBd userBD, MessageService messageService) {
        this.userBD = userBD;
        this.messageService = messageService;
    }

    public void criarUsuario(String numUser, String input, String idEmpresa) {
        try {
            UserDto usuario = new UserDto();
            usuario.setId_empresa(Integer.parseInt(idEmpresa));
            usuario.setNome("Usuario Teste");
            usuario.setTelefone("5511999999999");
            usuario.setPapel(Papel.funcionario);
            usuario.setFirst_contact(null);

            // Log do payload
            System.out.println("Payload createUser: " + mapper.writeValueAsString(usuario));

            userBD.createUser(usuario);
            messageService.sendMessage(numUser, "Usuário pré-definido criado com sucesso!");
        } catch (NumberFormatException nfe) {
            messageService.sendMessage(numUser, "ID da empresa inválido: " + idEmpresa);
        } catch (Exception e) {
            messageService.sendMessage(numUser, "Erro ao criar usuário: " + e.getMessage());
        }
    }

    public void listarUsuarios(String numUser, Integer idEmpresa) {
        try {
            String json = userBD.getUsuariosByEmpresa(idEmpresa);
            JsonNode root = mapper.readTree(json);
            JsonNode data = root.get("data");

            if (data == null || !data.isArray() || data.size() == 0) {
                messageService.sendMessage(numUser, "Nenhum usuário encontrado.");
                return;
            }

            StringBuilder out = new StringBuilder("Lista de Usuários\n\n");
            for (JsonNode user : data) {
                out.append("ID: ").append(user.path("id_user").asText()).append("\n")
                   .append("Nome: ").append(user.path("nome").asText()).append("\n")
                   .append("Telefone: ").append(user.path("telefone").asText()).append("\n")
                   .append("Papel: ").append(user.path("papel").asText()).append("\n")
                   .append("Atividade: ").append(user.path("atividade").asText()).append("\n")
                   .append("----------------------\n");
            }
            messageService.sendMessage(numUser, out.toString());

        } catch (Exception e) {
            messageService.sendMessage(numUser, "Erro ao listar usuários: " + e.getMessage());
        }
    }

    public void deletarUsuario(String numUser, Integer idUser) {
        try {
            String resposta = userBD.deleteUser(idUser);
            messageService.sendMessage(numUser, resposta);
        } catch (Exception e) {
            messageService.sendMessage(numUser, "Erro ao deletar usuário: " + e.getMessage());
        }
    }
}
