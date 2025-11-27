import { Request, Response } from "express"
import { getMensagens, enviarMensagem, enviarDocumento } from "../models/messageModel"
import { promises as fs} from 'fs'
import path from 'path'
import { v4 as uuidv4 } from 'uuid'

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
    let tempFilePath: string | null = null

    try {
        const { numero, documento, nomeArquivo } = req.body
        if (!numero || !documento || !nomeArquivo) {
            return res.status(400).json({error: "Informe número, documento (base64) e nome do arquivo" })
        }

        const tempDir = path.join(__dirname, '..', 'temp')
        await fs.mkdir(tempDir, { recursive: true })

        const uniqueFilename = `${uuidv4()}-${nomeArquivo}`
        tempFilePath = path.join(tempDir, uniqueFilename)

        const buffer = Buffer.from(documento, 'base64')
        await fs.writeFile(tempFilePath, buffer)

        await enviarDocumento(numero, tempFilePath, nomeArquivo)

        res.json({ sucesso: true })

    } catch (error) {
        console.error("Erro no processo de envio de documento:", error)
        res.status(500).json({ error: "Erro ao enviar documento" })
    } finally {
        if (tempFilePath) {
            try {
                await fs.unlink(tempFilePath)
                console.log(`Arquivo temporário ${tempFilePath} deletado.`)
            } catch (cleanupError) {
                console.error(`Erro ao deletar arquivo temporário ${tempFilePath}:`, cleanupError)
            }
        }
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
