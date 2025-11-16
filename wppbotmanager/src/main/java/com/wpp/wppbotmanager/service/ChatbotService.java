package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.dto.ReceiveMessageRequest;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChatbotService {

    private final UserStateManagerService userStateManager;
    private final MessageService messageService;
    private final EnviarResumoService enviarResumoService;

    public ChatbotService(UserStateManagerService userStateManager, MessageService messageService) {
        this.userStateManager = userStateManager;
        this.messageService = messageService;
        this.enviarResumoService = new EnviarResumoService(messageService);
    }

    private static final String TEXTO_MENU_PRINCIPAL =
            """
                    Olá, bem-vindo ao atendimento do Chatbot Easy!
                    Escolha uma opção:\s
                    1️⃣ - Resumo
                    2️⃣ - Relatório
                    3️⃣ - Gestão de Usuários""";

    private static final String TEXTO_MENU_RESUMO =
            """
                    Escolha um intervalo:
                    1️⃣ - 7 dias
                    2️⃣ - 15 dias
                    3️⃣ - 30 dias
                    4️⃣ - Mês atual
                    5️⃣ - Mês anterior
                    6️⃣ - Personalizado
                    0️⃣ - Voltar""";

    private static final String TEXTO_MENU_RELATORIO =
            """
                    Escolha um intervalo:
                    1️⃣ - 7 dias
                    2️⃣ - 15 dias
                    3️⃣ - 30 dias
                    4️⃣ - Mês atual
                    5️⃣ - Mês anterior
                    6️⃣ - Personalizado
                    0️⃣ - Voltar""";

    private static final String TEXTO_MENU_GESTAO_USUARIOS =
            """
                    1️⃣ - Cadastrar usuários
                    2️⃣ - Listar usuários
                    3️⃣ - Deletar usuários
                    0️⃣ - Voltar""";

    private static final Map<String, String> MAPA_MENU_PRINCIPAL = Map.of(
            "1", "SUBMENU_RESUMO",
            "2", "SUBMENU_RELATORIO",
            "3", "SUBMENU_GESTAO_USUARIOS"
    );

    private static final Map<String, String> MAPA_MENU_RESUMO = Map.of(
            "1", "7_DIAS_RESUMO",
            "2", "15_DIAS_RESUMO",
            "3", "30_DIAS_RESUMO",
            "4", "MES_ATUAL_RESUMO",
            "5", "MES_ANTERIOR_RESUMO",
            "6", "PERSONALIZADO_RESUMO",
            "0", UserStateManagerService.MENU_PRINCIPAL
    );

    private static final Map<String, String> MAPA_MENU_RELATORIO = Map.of(
            "1", "7_DIAS_RELATORIO",
            "2", "15_DIAS_RELATORIO",
            "3", "30_DIAS_RELATORIO",
            "4", "MES_ATUAL_RELATORIO",
            "5", "MES_ANTERIOR_RELATORIO",
            "6", "PERSONALIZADO_RELATORIO",
            "0", UserStateManagerService.MENU_PRINCIPAL
    );

    private static final Map<String, String> MAPA_MENU_GESTAO_USUARIOS = Map.of(
            "1", "CADASTRAR_USUARIOS",
            "2", "LISTAR_USUARIOS",
            "3", "DELETAR_USUARIOS",
            "0", UserStateManagerService.MENU_PRINCIPAL
    );
    public String analisarPapel(ReceiveMessageRequest request, String textInput) {
        if ("administrador".equalsIgnoreCase(request.getPapel())) {
            return MAPA_MENU_GESTAO_USUARIOS.getOrDefault(textInput, "ESTADO_INVALIDO");
        } else {
            messageService.sendMessage(request.getFrom(), "Acesso negado.");
            return "ACESSO_NEGADO";
        }
    }
    public void processMessage(ReceiveMessageRequest request, ReceiveReportRequest reportRequest) {
        String numUser = request.getFrom();
        String textInput = request.getTexto();
        String estadoAtual = userStateManager.getState(numUser);
        reportRequest.setIdEmpresa(request.getId_empresa());
        String proximoEstado;
        String resposta = "";

        switch (estadoAtual) {
            case UserStateManagerService.MENU_PRINCIPAL:
                if ("3".equals(textInput)) {
                    if ("administrador".equalsIgnoreCase(request.getPapel())) {
                        proximoEstado = "SUBMENU_GESTAO_USUARIOS";
                    } else {
                        messageService.sendMessage(numUser, "Função apenas para administradores.");
                        messageService.sendMessage(numUser, TEXTO_MENU_PRINCIPAL);
                        userStateManager.setState(numUser, UserStateManagerService.MENU_PRINCIPAL);
                        return;
                    }
                } else {
                    proximoEstado = MAPA_MENU_PRINCIPAL.getOrDefault(textInput, UserStateManagerService.MENU_PRINCIPAL);
                }
                break;
            case "SUBMENU_RELATORIO":
                proximoEstado = MAPA_MENU_RELATORIO.getOrDefault(textInput, "ESTADO_INVALIDO");
                break;
            case "SUBMENU_RESUMO":
                proximoEstado = MAPA_MENU_RESUMO.getOrDefault(textInput, "ESTADO_INVALIDO");

                int diasResumo = switch (textInput) {
                    case "1" -> 7;
                    case "2" -> 15;
                    case "3" -> 30;



                    case "6" -> -1;
                    default -> 0;
                };

                if (diasResumo > 0) {
                    messageService.sendMessage(numUser, "Gerando resumo de " + diasResumo + " dias...");
                    enviarResumoService.enviarRelatorio(numUser, diasResumo,null, null, reportRequest);
                    resposta = "";
                } else if ("0".equals(textInput)) {
                    proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
                    resposta = TEXTO_MENU_PRINCIPAL;
                } else if (diasResumo == -1) {
                    messageService.sendMessage(numUser, "Por favor, insira a data início (formato DD/MM/AAAA):");
                    proximoEstado = UserStateManagerService.INSERINDO_DATA_INICIO;
                    break;
                } else {
                    resposta = "Opção inválida!\n" + TEXTO_MENU_RESUMO;
                }
                break;
            case "SUBMENU_GESTAO_USUARIOS":
                proximoEstado = analisarPapel(request, textInput);
                break;
            case UserStateManagerService.INSERINDO_DATA_INICIO:
                userStateManager.setTempValue(numUser, "dataInicio", textInput);
                messageService.sendMessage(numUser, "Data início registrada: " + textInput);
                messageService.sendMessage(numUser, "Por favor, insira a data fim:");
                proximoEstado = UserStateManagerService.INSERINDO_DATA_FIM;
                break;

            case UserStateManagerService.INSERINDO_DATA_FIM:
                messageService.sendMessage(numUser, "Por favor, insira a data fim (formato DD/MM/AAAA):");
                userStateManager.setTempValue(numUser, "dataFim", textInput);
                messageService.sendMessage(numUser, "Data fim registrada: " + textInput);
                proximoEstado = UserStateManagerService.GERANDO_RESUMO_PERSONALIZADO;
                userStateManager.setState(numUser, proximoEstado);
                break;



            default:
                proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
        }
        switch (proximoEstado) {
            case UserStateManagerService.MENU_PRINCIPAL -> resposta = TEXTO_MENU_PRINCIPAL;
            case "SUBMENU_RESUMO" -> resposta = TEXTO_MENU_RESUMO;
            case "SUBMENU_RELATORIO" -> resposta = TEXTO_MENU_RELATORIO;
            case "SUBMENU_GESTAO_USUARIOS" -> resposta = TEXTO_MENU_GESTAO_USUARIOS;
            case "ACESSO_NEGADO" -> { return; }
            case "GERANDO_RESUMO_PERSONALIZADO" -> {
                String dataInicio = (String) userStateManager.getTempValue(numUser, "dataInicio");
                String dataFim = (String) userStateManager.getTempValue(numUser, "dataFim");
                if (dataInicio == null || dataFim == null) {
                    messageService.sendMessage(numUser, "Erro: datas não encontradas. Tente novamente.");
                    proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
                    break;
                }
                enviarResumoService.enviarRelatorio(numUser,0, dataInicio, dataFim, reportRequest);
                messageService.sendMessage(numUser, TEXTO_MENU_PRINCIPAL);
                proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
            }
            case "ESTADO_INVALIDO" -> {
                resposta = "Opção inválida!\n" + TEXTO_MENU_PRINCIPAL;
                proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
            }
        }
        if (!resposta.isBlank()) {
            messageService.sendMessage(numUser, resposta);
        }
        userStateManager.setState(numUser, proximoEstado);
    }
    public void inactiveUser(String numUser) {
        messageService.sendMessage(numUser, "Contate um administrador para reativar seu acesso.");
    }
    public void unknownUser(String numUser) {
        messageService.sendMessage(numUser, "Usuário não encontrado. Contate um administrador.");
    }
}
