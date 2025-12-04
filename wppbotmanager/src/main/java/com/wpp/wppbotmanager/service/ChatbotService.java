package com.wpp.wppbotmanager.service;

import com.wpp.wppbotmanager.dto.UserDto;
import com.wpp.wppbotmanager.dto.ReceiveMessageRequest;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;
import com.wpp.wppbotmanager.dto.enums.papel;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatbotService {

    private final UserStateManagerService userStateManager;
    private final MessageService messageService;
    private final EnviarResumoService enviarResumoService;
    private final UserChatService userChatService;
    private final EnviarRelatorioService enviarRelatorioService;

    private final Map<String, UserDto> criarTemp = new ConcurrentHashMap<>();

    public ChatbotService(UserStateManagerService userStateManager, MessageService messageService, UserChatService userChatService, EnviarRelatorioService enviarRelatorioService) {
        this.userStateManager = userStateManager;
        this.messageService = messageService;
        this.enviarResumoService = new EnviarResumoService(messageService);
        this.userChatService = userChatService;
        this.enviarRelatorioService = enviarRelatorioService;
    }

    private static final String TEXTO_MENU_PRINCIPAL =
            """
                    > Olá, sou o Chatbot Easy! O que você gostaria de fazer?
                    
                    1️⃣ - *Visualizar Resumo Financeiro (na Tela)*
                    2️⃣ - *Gerar Relatório Financeiro (PDF)*
                    3️⃣ - *Gerenciar Usuários da Plataforma*
                    0️⃣ - *Sair*""";

    private static final String TEXTO_MENU_RESUMO =
            """
                    > *Resumo Financeiro - Escolha um intervalo:*
                    
                    1️⃣ - *7 dias*
                    2️⃣ - *15 dias*
                    3️⃣ - *30 dias*
                    4️⃣ - *Mês atual*
                    5️⃣ - *Mês anterior*
                    6️⃣ - *Personalizado*
                    0️⃣ - *Voltar*""";

    private static final String TEXTO_MENU_RELATORIO =
            """
                    > *Relatório Financeiro - Escolha um intervalo:*
                    
                    1️⃣ - *7 dias*
                    2️⃣ - *15 dias*
                    3️⃣ - *30 dias*
                    4️⃣ - *Mês atual*
                    5️⃣ - *Mês anterior*
                    6️⃣ - *Personalizado*
                    0️⃣ - *Voltar*""";

    private static final String TEXTO_MENU_GESTAO_USUARIOS =
            """     
                    > *Gestão de Usuários - Escolha uma opção:*
                    
                    1️⃣ - *Cadastrar usuários*
                    2️⃣ - *Listar usuários*
                    3️⃣ - *Deletar usuários*
                    0️⃣ - *Voltar*""";

    private static final Map<String, String> MAPA_MENU_PRINCIPAL = Map.of(
            "1", "SUBMENU_RESUMO",
            "2", "SUBMENU_RELATORIO",
            "3", "SUBMENU_GESTAO_USUARIOS",
            "0", "SAIR"
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

    public void processMessage(ReceiveMessageRequest request, ReceiveReportRequest reportRequest) {
        String numUser = request.getFrom();
        String textInput = request.getTexto();
        String estadoAtual = userStateManager.getState(numUser);
        reportRequest.setIdEmpresa(request.getId_empresa());
        String proximoEstado;

        switch (estadoAtual) {
            case UserStateManagerService.PRIMEIRO_CONTATO -> proximoEstado = UserStateManagerService.PRIMEIRO_CONTATO;

            case UserStateManagerService.MENU_PRINCIPAL -> proximoEstado = MAPA_MENU_PRINCIPAL.getOrDefault(textInput, "ESTADO_INVALIDO");

            case "SUBMENU_RESUMO" -> proximoEstado = MAPA_MENU_RESUMO.getOrDefault(textInput, "ESTADO_INVALIDO");

            case UserStateManagerService.INSERINDO_DATA_INICIO -> {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                try {
                    LocalDate.parse(textInput, fmt); // valida
                } catch (Exception e) {
                    messageService.sendMessage(numUser, "Data inválida. Use o formato DD/MM/AAAA.");
                    proximoEstado = UserStateManagerService.INSERINDO_DATA_INICIO;
                    return;
                }
                userStateManager.setTempValue(numUser, "dataInicio", textInput);
                messageService.sendMessage(numUser, "Data inicial registrada: " + textInput);
                messageService.sendMessage(numUser, "Por favor, insira a data final (formato DD/MM/AAAA):");
                proximoEstado = UserStateManagerService.INSERINDO_DATA_FIM;
            }

            case UserStateManagerService.INSERINDO_DATA_FIM -> {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                try {
                    LocalDate.parse(textInput, fmt); // valida
                } catch (Exception e) {
                    messageService.sendMessage(numUser, "Data inválida. Use o formato DD/MM/AAAA.");
                    proximoEstado = UserStateManagerService.INSERINDO_DATA_FIM;
                    return;
                }
                userStateManager.setTempValue(numUser, "dataFim", textInput);
                messageService.sendMessage(numUser, "Data final registrada: " + textInput);
                proximoEstado = UserStateManagerService.GERANDO_RESUMO_PERSONALIZADO;
            }

            case "SUBMENU_RELATORIO" -> proximoEstado = MAPA_MENU_RELATORIO.getOrDefault(textInput, "ESTADO_INVALIDO");

            case UserStateManagerService.INSERINDO_DATA_INICIO_RELATORIO -> {
                userStateManager.setTempValue(numUser, "dataInicioRelatorio", textInput);
                messageService.sendMessage(numUser, "Data inicial registrada: " + textInput);
                messageService.sendMessage(numUser, "Por favor, insira a data final (formato DD/MM/AAAA):");
                proximoEstado = UserStateManagerService.INSERINDO_DATA_FIM_RELATORIO;
            }

            case UserStateManagerService.INSERINDO_DATA_FIM_RELATORIO -> {
                userStateManager.setTempValue(numUser, "dataFimRelatorio", textInput);
                messageService.sendMessage(numUser, "Data final registrada: " + textInput);
                proximoEstado = UserStateManagerService.GERANDO_RELATORIO_PERSONALIZADO;
            }

            case "SUBMENU_GESTAO_USUARIOS" -> proximoEstado = MAPA_MENU_GESTAO_USUARIOS.getOrDefault(textInput, "ESTADO_INVALIDO");

            case UserStateManagerService.AGUARDAR_NOME_CADASTRO -> {
                UserDto temp = criarTemp.get(numUser);
                temp.setNome(textInput);
                messageService.sendMessage(numUser, "Digite o telefone do usuário (ex: 551199999999):");
                proximoEstado = UserStateManagerService.AGUARDAR_TELEFONE_CADASTRO;
            }

            case UserStateManagerService.AGUARDAR_TELEFONE_CADASTRO -> {
                if (!textInput.matches("\\d{12}")) {
                    messageService.sendMessage(numUser, "Telefone inválido. Digite exatamente 12 números.");
                    proximoEstado = UserStateManagerService.AGUARDAR_TELEFONE_CADASTRO;
                    return;
                }
                UserDto temp = criarTemp.get(numUser);
                temp.setTelefone(textInput);
                proximoEstado = UserStateManagerService.FINALIZAR_CADASTRO;
            }

            case UserStateManagerService.AGUARDAR_ID_DELETAR -> {
                try {
                    Integer idEscolhido = Integer.parseInt(textInput.trim());
                    userChatService.marcarUsuarioInativo(numUser, idEscolhido);
                    messageService.sendMessage(numUser, "Pronto! O usuário foi excluído. Deseja algo mais?");
                    proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
                } catch (NumberFormatException e) {
                    messageService.sendMessage(numUser, "ID inválido. Digite somente números:");
                    proximoEstado = UserStateManagerService.AGUARDAR_ID_DELETAR;
                }
            }

            case UserStateManagerService.AGUARDANDO_CONTINUACAO -> {
                if ("sim".equalsIgnoreCase(textInput) || "s".equalsIgnoreCase(textInput)) {
                    proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
                } else if ("nao".equalsIgnoreCase(textInput) || "n".equalsIgnoreCase(textInput) || "não".equalsIgnoreCase(textInput)) {
                    messageService.sendMessage(numUser, "Entendido. Obrigado pelo contato!");
                    userStateManager.setState(numUser, UserStateManagerService.PRIMEIRO_CONTATO);
                    return;
                } else {
                    messageService.sendMessage(numUser, "Desculpe, não entendi. Deseja continuar? (Sim/Não)");
                    proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
                }
            }

            default -> proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
        }

        switch (proximoEstado) {
            case UserStateManagerService.PRIMEIRO_CONTATO -> {
                messageService.sendMessage(numUser, TEXTO_MENU_PRINCIPAL);
                proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
            }

            case UserStateManagerService.MENU_PRINCIPAL -> messageService.sendMessage(numUser, TEXTO_MENU_PRINCIPAL);

            case "SAIR" -> {
                messageService.sendMessage(numUser, "Obrigado pelo contato!");
                proximoEstado = UserStateManagerService.PRIMEIRO_CONTATO;
            }

            case "SUBMENU_RESUMO" -> messageService.sendMessage(numUser, TEXTO_MENU_RESUMO);

            case "7_DIAS_RESUMO" -> {
                messageService.sendMessage(numUser, "Gerando resumo de 7 dias...");
                enviarResumoService.enviarRelatorio(numUser, 7, null, null, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O resumo de 7 dias foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "15_DIAS_RESUMO" -> {
                messageService.sendMessage(numUser, "Gerando resumo de 15 dias...");
                enviarResumoService.enviarRelatorio(numUser, 15, null, null, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O resumo de 15 dias foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "30_DIAS_RESUMO" -> {
                messageService.sendMessage(numUser, "Gerando resumo de 30 dias...");
                enviarResumoService.enviarRelatorio(numUser, 30, null, null, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O resumo de 30 dias foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "MES_ATUAL_RESUMO" -> {
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dataInicial = LocalDate.now().withDayOfMonth(1);
                LocalDate dataFinal = LocalDate.now();
                String dataInicialFormatada = dataInicial.format(formatador);
                String dataFinalFormatada = dataFinal.format(formatador);
                enviarResumoService.enviarRelatorio(numUser, 0, dataInicialFormatada, dataFinalFormatada, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O resumo do mês atual foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "MES_ANTERIOR_RESUMO" -> {
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate hoje = LocalDate.now();
                LocalDate mesAnterior = hoje.minusMonths(1);
                LocalDate dataInicial = mesAnterior.withDayOfMonth(1);
                LocalDate dataFinal = mesAnterior.with(TemporalAdjusters.lastDayOfMonth());
                String dataInicialFormatada = dataInicial.format(formatador);
                String dataFinalFormatada = dataFinal.format(formatador);
                enviarResumoService.enviarRelatorio(numUser, 0, dataInicialFormatada, dataFinalFormatada, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O resumo do mês anterior foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "PERSONALIZADO_RESUMO" -> {
                messageService.sendMessage(numUser, "Por favor, insira a data inicial (formato DD/MM/AAAA):");
                proximoEstado = UserStateManagerService.INSERINDO_DATA_INICIO;
            }

            case UserStateManagerService.GERANDO_RESUMO_PERSONALIZADO -> {
                String dataInicio = (String) userStateManager.getTempValue(numUser, "dataInicio");
                String dataFim = (String) userStateManager.getTempValue(numUser, "dataFim");
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate inicio = LocalDate.parse(dataInicio, fmt);
                LocalDate fim = LocalDate.parse(dataFim, fmt);
                long diff = ChronoUnit.DAYS.between(inicio, fim);
                if(diff < 0){
                    messageService.sendMessage(numUser, "A data final é menor que a inicial. Por favor, insira novamente a data inicial (formato DD/MM/AAAA):");
                    proximoEstado = UserStateManagerService.INSERINDO_DATA_INICIO;
                }
                else {
                    enviarResumoService.enviarRelatorio(numUser,0, dataInicio, dataFim, reportRequest);
                    messageService.sendMessage(numUser, "Pronto! O resumo de " + dataInicio + " até " + dataFim + " foi enviado. Deseja algo mais?");
                    proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
                }
            }

            case "SUBMENU_RELATORIO" -> messageService.sendMessage(numUser, TEXTO_MENU_RELATORIO);

            case "7_DIAS_RELATORIO" -> {
                messageService.sendMessage(numUser, "Gerando relatório de 7 dias...");
                LocalDate dataFim = LocalDate.now();
                LocalDate dataInicio = dataFim.minusDays(6);
                DateTimeFormatter formatadorApi = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dataInicioFormatada = dataInicio.format(formatadorApi);
                String dataFimFormatada = dataFim.format(formatadorApi);
                enviarRelatorioService.enviarRelatorioGerado(numUser, dataInicioFormatada, dataFimFormatada, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O relatório de " + dataInicioFormatada + " até " + dataFimFormatada + " foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "15_DIAS_RELATORIO" -> {
                messageService.sendMessage(numUser, "Gerando relatório de 15 dias...");
                LocalDate dataFim = LocalDate.now();
                LocalDate dataInicio = dataFim.minusDays(14);
                DateTimeFormatter formatadorApi = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dataInicioFormatada = dataInicio.format(formatadorApi);
                String dataFimFormatada = dataFim.format(formatadorApi);
                enviarRelatorioService.enviarRelatorioGerado(numUser, dataInicioFormatada, dataFimFormatada, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O relatório de " + dataInicioFormatada + " até " + dataFimFormatada + " foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "30_DIAS_RELATORIO" -> {
                messageService.sendMessage(numUser, "Gerando relatório de 30 dias...");
                LocalDate dataFim = LocalDate.now();
                LocalDate dataInicio = dataFim.minusDays(29);
                DateTimeFormatter formatadorApi = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dataInicioFormatada = dataInicio.format(formatadorApi);
                String dataFimFormatada = dataFim.format(formatadorApi);
                enviarRelatorioService.enviarRelatorioGerado(numUser, dataInicioFormatada, dataFimFormatada, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O relatório de " + dataInicioFormatada + " até " + dataFimFormatada + " foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "MES_ATUAL_RELATORIO" -> {
                messageService.sendMessage(numUser, "Gerando relatório do mês atual...");
                DateTimeFormatter formatadorApi = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dataInicio = LocalDate.now().withDayOfMonth(1);
                LocalDate dataFim = LocalDate.now();
                String dataInicioFormatada = dataInicio.format(formatadorApi);
                String dataFimFormatada = dataFim.format(formatadorApi);
                enviarRelatorioService.enviarRelatorioGerado(numUser, dataInicioFormatada, dataFimFormatada, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O relatório do mês atual (" + dataInicioFormatada + " a " + dataFimFormatada + ") foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "MES_ANTERIOR_RELATORIO" -> {
                messageService.sendMessage(numUser, "Gerando relatório do mês anterior...");
                DateTimeFormatter formatadorApi = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate mesAnterior = LocalDate.now().minusMonths(1);
                LocalDate dataInicio = mesAnterior.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate dataFim = mesAnterior.with(TemporalAdjusters.lastDayOfMonth());
                String dataInicioFormatada = dataInicio.format(formatadorApi);
                String dataFimFormatada = dataFim.format(formatadorApi);
                enviarRelatorioService.enviarRelatorioGerado(numUser, dataInicioFormatada, dataFimFormatada, reportRequest);
                messageService.sendMessage(numUser, "Pronto! O relatório do mês anterior (" + dataInicioFormatada + " a " + dataFimFormatada + ") foi enviado. Deseja algo mais?");
                proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
            }

            case "PERSONALIZADO_RELATORIO" -> {
                messageService.sendMessage(numUser, "Para o relatório personalizado, por favor, insira a data inicial (formato DD/MM/AAAA):");
                proximoEstado = UserStateManagerService.INSERINDO_DATA_INICIO_RELATORIO;
            }

            case UserStateManagerService.GERANDO_RELATORIO_PERSONALIZADO -> {
                String dataInicio = (String) userStateManager.getTempValue(numUser, "dataInicioRelatorio");
                String dataFim = (String) userStateManager.getTempValue(numUser, "dataFimRelatorio");
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate inicio = LocalDate.parse(dataInicio, fmt);
                LocalDate fim = LocalDate.parse(dataFim, fmt);
                long diff = ChronoUnit.DAYS.between(inicio, fim);

                if (diff < 0) {
                    messageService.sendMessage(numUser, "A data final é menor que a inicial. Por favor, insira novamente a data inicial (formato DD/MM/AAAA).");
                    proximoEstado = UserStateManagerService.INSERINDO_DATA_INICIO_RELATORIO;
                } else {
                    messageService.sendMessage(numUser, "Gerando relatório de " + dataInicio + " até " + dataFim + "...");
                    enviarRelatorioService.enviarRelatorioGerado(numUser, dataInicio, dataFim, reportRequest);
                    messageService.sendMessage(numUser, "Pronto! O relatório personalizado foi enviado. Deseja algo mais?");
                    proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
                }
            }

            case "SUBMENU_GESTAO_USUARIOS" -> {
                if ("administrador".equalsIgnoreCase(request.getPapel())) {
                    messageService.sendMessage(numUser, TEXTO_MENU_GESTAO_USUARIOS);
                    proximoEstado = "SUBMENU_GESTAO_USUARIOS";
                } else {
                    messageService.sendMessage(numUser, "O acesso a esta funcionalidade é restrito a usuários administradores.");
                    proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
                }
            }

            case "CADASTRAR_USUARIOS" -> {
                UserDto u = new UserDto();
                try {
                    if (request.getId_empresa() != null && !request.getId_empresa().isEmpty())
                        u.setId_empresa(Integer.parseInt(request.getId_empresa()));
                } catch (NumberFormatException ignored) {}
                u.setPapel(papel.Papel.funcionario);
                criarTemp.put(numUser, u);
                messageService.sendMessage(numUser, "Digite o nome do usuário:");
                proximoEstado = UserStateManagerService.AGUARDAR_NOME_CADASTRO;
            }

            case UserStateManagerService.FINALIZAR_CADASTRO -> {
                UserDto temp = criarTemp.get(numUser);
                try {
                    userChatService.salvarUsuario(temp);
                    messageService.sendMessage(numUser, "Pronto! O usuário foi criado com sucesso. Deseja algo mais?");
                } catch (Exception e) {
                    messageService.sendMessage(numUser, "Erro ao criar usuário: " + e.getMessage());
                } finally {
                    criarTemp.remove(numUser);
                    proximoEstado = UserStateManagerService.AGUARDANDO_CONTINUACAO;
                }
            }

            case "LISTAR_USUARIOS" -> {
                userChatService.listarUsuarios(numUser, Integer.parseInt(request.getId_empresa()));
                messageService.sendMessage(numUser, "Pronto! Os usuários foram listados. Deseja algo mais?");
            }

            case "DELETAR_USUARIOS" -> {
                userChatService.listarUsuarios(numUser, Integer.parseInt(request.getId_empresa()));
                messageService.sendMessage(numUser, "Digite o ID do usuário que deseja excluir:");
                proximoEstado = UserStateManagerService.AGUARDAR_ID_DELETAR;
            }

            case "ESTADO_INVALIDO" -> {
                messageService.sendMessage(numUser, "Opção inválida!");
                switch (estadoAtual) {
                    case "SUBMENU_RESUMO" -> {
                        messageService.sendMessage(numUser, TEXTO_MENU_RESUMO);
                        proximoEstado = "SUBMENU_RESUMO";
                    }

                    case "SUBMENU_RELATORIO" -> {
                        messageService.sendMessage(numUser, TEXTO_MENU_RELATORIO);
                        proximoEstado = "SUBMENU_RELATORIO";
                    }

                    case "SUBMENU_GESTAO_USUARIOS" -> {
                        messageService.sendMessage(numUser, TEXTO_MENU_GESTAO_USUARIOS);
                        proximoEstado = "SUBMENU_GESTAO_USUARIOS";
                    }

                    default -> {
                        messageService.sendMessage(numUser, TEXTO_MENU_PRINCIPAL);
                        proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
                    }
                }
            }
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