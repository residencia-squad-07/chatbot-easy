import { Request, Response } from "express"
import { getMensagens, enviarMensagem, enviarDocumento } from "../models/messageModel"

export async function sendMessage(req: Request, res: Response) {
  try {
    const { numero, texto } = req.body
    if (!numero || !texto) return res.status(400).json({ error: "Informe número e texto" })

    await enviarMensagem(numero, texto)
    res.json({ sucesso: true })
  } catch (error) {
    console.error(error)
    res.status(500).json({ error: "Erro ao enviar mensagem" })
  }
}

export async function sendDocument(req: Request, res: Response) {
    try {
        const { numero, documento, nomeArquivo } = req.body;
        if (!numero || !documento || !nomeArquivo) {
            return res.status(400).json({error: "Informe número, documento (base64) e nome do arquivo" });
        }

        const buffer = Buffer.from(documento, 'base64');

        await enviarDocumento(documento, buffer, nomeArquivo);
        res.json({ sucesso: true });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Erro ao enviar documento" });
    }
}

export async function listMessage(req: Request, res: Response) {
  try {
    const mensagens = getMensagens();
    res.json(mensagens);
  } catch (error) {
    res.status(500).json({ error: "Erro ao buscar mensagens" });
  }
}
