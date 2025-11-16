package com.wpp.wppbotmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpp.wppbotmanager.dto.ReceiveMessageRequest;
import com.wpp.wppbotmanager.dto.ReceiveReportRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    private final UserStateManagerService userStateManager;
    private final MessageService messageService;

    public ChatbotService(UserStateManagerService userStateManager, MessageService messageService) {
        this.userStateManager = userStateManager;
        this.messageService = messageService;
    }
    private JsonNode unwrapMessageEnvelope(JsonNode node) {
        if (node == null || node.isNull()) return node;
        JsonNode cur = node;
        while (cur != null && cur.has("message")) {
            cur = cur.get("message");
        }
        if (cur != null && cur.isArray() && cur.size() > 0) {
            return cur.get(0);
        }
        return cur;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void enviarRelatorio(String numUser, int dias,String dataInicio, String dataFim, ReceiveReportRequest reportRequest) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String idEmpresa = reportRequest.getIdEmpresa();
            if (idEmpresa == null || idEmpresa.isBlank()) {
                messageService.sendMessage(numUser, "Nenhuma empresa associada ao seu usuário.");
                return;
            }

            String empresaUrl = "http://localhost:3001/empresa/listarempresa/" + idEmpresa;
            String empresaJson = restTemplate.getForObject(empresaUrl, String.class);
            if (empresaJson == null || empresaJson.isBlank()) {
                messageService.sendMessage(numUser, "Empresa não encontrada.");
                return;
            }

            JsonNode empresaNode = objectMapper.readTree(empresaJson);
            empresaNode = unwrapMessageEnvelope(empresaNode);

            if (empresaNode == null || empresaNode.isNull()) {
                messageService.sendMessage(numUser, "Erro ao interpretar dados da empresa.");
                return;
            }

            if (empresaNode.isArray() && empresaNode.size() > 0) {
                empresaNode = empresaNode.get(0);
            }

            String appKey = empresaNode.path("app_key").asText(empresaNode.path("appKey").asText(null));
            String appSecret = empresaNode.path("app_secret").asText(empresaNode.path("appSecret").asText(null));

            if (appKey == null || appSecret == null) {
                messageService.sendMessage(numUser, "Dados de integração não encontrados.");
                return;
            }

            String url;
            if (dias != 0) {
                url = "http://localhost:3002/relatorio/resumo_geral/dias/" + dias;
            } else {
                url = "http://localhost:3001/omie/relatorio-financeiro?data_inicio=" + dataInicio + "&data_fim=" + dataFim;
            }

            Map<String, Object> body = new HashMap<>();
            body.put("appKey", appKey);
            body.put("appSecret", appSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(url, entity, String.class);
            if (response == null) {
                messageService.sendMessage(numUser, "Erro ao gerar relatório.");
                return;
            }
            try {
                JsonNode jsonResponse = objectMapper.readTree(response);
                JsonNode resumo = jsonResponse.path("resumo_geral");
                JsonNode periodo = resumo.path("periodo_analisado");
                int totalDias = periodo.path("total_dias").asInt(0);
                String totalReceitas = resumo.path("total_receitas").asText("");
                String totalDespesas = resumo.path("total_despesas_custos").asText("");
                String resultado = resumo.path("resultado_liquido").asText("");
                JsonNode cat = jsonResponse.path("detalhes_por_categoria");
                String out = "📊 *Resumo Geral – Período Analisado*\n" +
                        "🗓️ De: " + dataInicio + "\n" +
                        "🗓️ Até: " + dataFim + "\n\n" +
                        "📅 *Duração:* " + totalDias + " dias\n\n" +
                        "💰 *Totais*\n\n" +
                        "Receitas: " + totalReceitas + "\n\n" +
                        "Despesas / Custos: " + totalDespesas + "\n\n" +
                        "Resultado Líquido: " + resultado + "\n\n" +
                        "📂 *Detalhamento por Categoria*\n\n" +
                        "Receitas\n\n" +
                        "Receitas Operacionais: " + cat.path("receitas_operacionais").asText("") + "\n" +
                        "Entradas Não Operacionais: " + cat.path("entradas_nao_operacionais").asText("") + "\n\n" +
                        "Despesas e Custos\n\n" +
                        "Custos Variáveis: " + cat.path("custos_variaveis").asText("") + "\n" +
                        "Despesas com Pessoal: " + cat.path("despesas_com_pessoal").asText("") + "\n" +
                        "Despesas Administrativas: " + cat.path("despesas_administrativas").asText("") + "\n" +
                        "Pró-labore: " + cat.path("pro_labore").asText("") + "\n" +
                        "Investimentos: " + cat.path("investimentos").asText("") + "\n" +
                        "Parcelamentos: " + cat.path("parcelamentos").asText("") + "\n" +
                        "Saídas Não Operacionais: " + cat.path("saidas_nao_operacionais").asText("") + "\n";
                messageService.sendMessage(numUser, out);

            } catch (Exception e) {
                messageService.sendMessage(numUser, response);
            }

        } catch (Exception e) {
            messageService.sendMessage(numUser, "Erro interno: " + e.getMessage());
        }
    }


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
                    case "4" -> 4;
                    case "5" -> 5;
                    case "6" -> -1;
                    default -> 0;
                };

                if (diasResumo == 4) {
                    messageService.sendMessage(numUser, "Gerando resumo do mês atual...");
                } else if (diasResumo == 5) {
                    messageService.sendMessage(numUser, "Gerando resumo do mês anterior...");
                } else if (diasResumo > 0) {
                    messageService.sendMessage(numUser, "Gerando resumo de " + diasResumo + " dias...");
                    enviarRelatorio(numUser, diasResumo,null, null, reportRequest);
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
            case "MES_ATUAL_RESUMO" -> {
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dataInicial = LocalDate.now().withDayOfMonth(1);
                LocalDate dataFinal = LocalDate.now();
                String dataInicialFormatada = dataInicial.format(formatador);
                String dataFinalFormatada = dataFinal.format(formatador);
                enviarRelatorio(numUser, 0, dataInicialFormatada, dataFinalFormatada, reportRequest);
                messageService.sendMessage(numUser, TEXTO_MENU_PRINCIPAL);
                proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
            }
            case "ACESSO_NEGADO" -> { return; }
            case "GERANDO_RESUMO_PERSONALIZADO" -> {
                String dataInicio = (String) userStateManager.getTempValue(numUser, "dataInicio");
                String dataFim = (String) userStateManager.getTempValue(numUser, "dataFim");
                if (dataInicio == null || dataFim == null) {
                    messageService.sendMessage(numUser, "Erro: datas não encontradas. Tente novamente.");
                    proximoEstado = UserStateManagerService.MENU_PRINCIPAL;
                    break;
                }
                enviarRelatorio(numUser,0, dataInicio, dataFim, reportRequest);
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
