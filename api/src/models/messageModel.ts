import { getSock } from "../utils/baileys";

export async function enviarMensagem(numero: string, texto: string) {
  const sock = getSock();
  if (!sock) throw new Error("Bot ainda não está conectado");

  await sock.sendMessage(`${numero}@s.whatsapp.net`, { text: texto });
}

export async function enviarDocumento(numero: string, filePath: string, nomeArquivo: string) {
    const sock = await getSock();
    if (!sock) throw new Error("Bot ainda não está conectado");

    console.log('Enviando documento ${nomeArquivo} para ${numero} a partir de ${filePath}');

    await sock.sendMessage(`${numero}@s.whatsapp.net`, {
        document: { url: filePath },
        mimetype: 'application/pdf',
        fileName: nomeArquivo
    });
}


let mensagens: any[] = [];

export function addMensagem(msg: any) {
  mensagens.push(msg);
}

export function getMensagens() {
  return mensagens;
}

export function clearMensagens() {
  mensagens = [];
}