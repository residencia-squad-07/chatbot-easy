import { Request, Response } from "express";
import * as omieService from "../services/omieService";

const formatadorReais = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const MAPA_DESCRICOES_RESPOSTA: Record<string, string> = {
  "1.0": "receitas_operacionais",
  "2.1": "custos_variaveis",
  "3.0": "despesas_com_pessoal",
  "3.1": "despesas_administrativas",
  "3.2": "pro_labore",
  "4.0": "investimentos",
  "5.0": "parcelamentos",
  "6.0": "entradas_nao_operacionais",
  "7.0": "saidas_nao_operacionais"
};

const ehReceita = (codigo: string): boolean => codigo.startsWith("1.") || codigo.startsWith("6.");
const ehDespesaOuCusto = (codigo: string): boolean =>
    codigo.startsWith("2.") || codigo.startsWith("3.") || codigo.startsWith("5.") || codigo.startsWith("7.");

function parseDateBrasileira(dateString: string): Date | null {
  const [dia, mes, ano] = dateString.split('/').map(Number);
  if (dia && mes && ano && dateString.length === 10 && ano > 1000) {
    return new Date(Date.UTC(ano, mes - 1, dia, 0, 0, 0));
  }
  return null;
}
function formatDateBrasileira(date: Date): string {
  const dia = String(date.getUTCDate()).padStart(2, '0');
  const mes = String(date.getUTCMonth() + 1).padStart(2, '0');
  const ano = date.getUTCFullYear();
  return `${dia}/${mes}/${ano}`;
}

export async function gerarRelatorioFinanceiroGeralController(req: Request, res: Response) {
  try {

    const { appKey, appSecret } = req.body;
    if (!appKey || !appSecret) {
      return res.status(400).json({ error: 'Os campos appKey e appSecret são obrigatórios no corpo da requisição.' });
    }

    const { dias, data_inicio, data_fim, mes_passado } = req.query;

    let dataInicio: Date;
    let dataFim: Date;
    
    const dataAtual = new Date();
    const agoraUTC = new Date(Date.UTC(dataAtual.getUTCFullYear(), dataAtual.getUTCMonth(), dataAtual.getUTCDate()));

    if (data_inicio && data_fim) {
        
        const tempInicio = parseDateBrasileira(data_inicio as string);
        const tempFim = parseDateBrasileira(data_fim as string);

        if (!tempInicio || !tempFim) {
             return res.status(400).json({ error: 'Datas devem estar no formato dd/mm/AAAA.' });
        }
        
        dataInicio = tempInicio;
        dataFim = tempFim;
        
        if (dataFim.getTime() < dataInicio.getTime()) {
            return res.status(400).json({ error: 'A data final não pode ser menor que a data inicial.' });
        }

        dataFim.setUTCHours(23, 59, 59, 999);
    
    } else if ((mes_passado as string)?.toLowerCase() === 'true') {
        const primeiroDiaMesAtualUTC = new Date(Date.UTC(agoraUTC.getUTCFullYear(), agoraUTC.getUTCMonth(), 1));
        dataFim = new Date(primeiroDiaMesAtualUTC.getTime() - 1); 
        dataInicio = new Date(Date.UTC(dataFim.getUTCFullYear(), dataFim.getUTCMonth(), 1));
        
    } else {
        const diasNum = parseInt(dias as string) || 1825; 

        if (diasNum <= 0) {
            return res.status(400).json({ error: 'O número de dias deve ser positivo.' });
        }

        dataFim = new Date(agoraUTC.getTime());
        dataFim.setUTCHours(23, 59, 59, 999); 
        dataInicio = new Date(agoraUTC.getTime());
        dataInicio.setUTCDate(agoraUTC.getUTCDate() - (diasNum - 1)); 
        dataInicio.setUTCHours(0, 0, 0, 0); 
    }

    const resultadosNumericos = await omieService.gerarRelatorioFinanceiroGeral(appKey, appSecret, dataInicio, dataFim);

    let totalReceitas = 0;
    let totalDespesasCustos = 0;
    const detalhesFormatados: Record<string, string> = {};

    for (const codigo in resultadosNumericos) {
        if (resultadosNumericos.hasOwnProperty(codigo)) {
            const valor = resultadosNumericos[codigo];
            const descricaoChave = MAPA_DESCRICOES_RESPOSTA[codigo] || codigo;
            detalhesFormatados[descricaoChave] = formatadorReais.format(valor);

            if (ehReceita(codigo)) {
                totalReceitas += valor;
            } else if (ehDespesaOuCusto(codigo)) {
                totalDespesasCustos += valor;
            }
        }
    }

    const resultadoLiquido = totalReceitas - totalDespesasCustos;
    
    const dataFimStart = new Date(Date.UTC(dataFim.getUTCFullYear(), dataFim.getUTCMonth(), dataFim.getUTCDate()));
    const diffTime = dataFimStart.getTime() - dataInicio.getTime();
    const totalDias = (diffTime / (1000 * 60 * 60 * 24)) + 1;

    const respostaFinal = {
      resumo_geral: {
        periodo_analisado: {
          data_inicio: formatDateBrasileira(dataInicio),
          data_fim: formatDateBrasileira(dataFim),
          total_dias: totalDias
        },
        total_receitas: formatadorReais.format(totalReceitas),
        total_despesas_custos: formatadorReais.format(totalDespesasCustos),
        resultado_liquido: formatadorReais.format(resultadoLiquido)
      },
      detalhes_por_categoria: detalhesFormatados
    };

    return res.json(respostaFinal);

  } catch (err: any) {
    console.error("ERRO AO GERAR RELATÓRIO GERAL:", err.response?.data || err.message || err);
    return res.status(500).json({
        error: 'Falha ao processar a solicitação.',
        details: err.response?.data?.faultstring || err.response?.data?.error || err.message || 'Erro desconhecido'
    });
  }
}