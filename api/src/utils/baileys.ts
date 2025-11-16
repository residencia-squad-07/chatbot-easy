// utils/whatsapp.ts
import makeWASocket, {
  useMultiFileAuthState,
  fetchLatestBaileysVersion,
  DisconnectReason,
  jidNormalizedUser,
  proto
} from "@whiskeysockets/baileys";
import pino from "pino";
import qrcode from "qrcode-terminal";
import { Boom } from "@hapi/boom";
import { addMensagem } from "../models/messageModel";
import moment from "moment-timezone";
import Usuario from "../models/userModel"

let globalSock: ReturnType<typeof makeWASocket> | null = null;

export async function connectToWhatsApp() {
  const { state, saveCreds } = await useMultiFileAuthState("auth_info_bot");
  const { version } = await fetchLatestBaileysVersion();

  const sock = makeWASocket({
    version,
    auth: state,
    printQRInTerminal: false,
    browser: ["BOT_API", "Chrome", "10.0"],
    logger: pino({ level: "silent" }),
  });

  globalSock = sock;

  sock.ev.on("connection.update", (update) => {
    const { connection, qr, lastDisconnect } = update;
    if (qr) {
      qrcode.generate(qr, { small: true });
    }
    if (connection === "open") {
      console.log("BOT CONECTADO ✅");
    }
    if (connection === "close") {
      const statusCode = (lastDisconnect?.error as Boom)?.output?.statusCode;
      const reconnect = statusCode !== DisconnectReason.loggedOut;
      if (reconnect) connectToWhatsApp();
    }
  });

  sock.ev.on("creds.update", saveCreds);

  sock.ev.on("messages.upsert", async ({ messages }) => {
    const msg = messages[0];
    if (!msg.message || msg.key.fromMe) return;
    if (msg.key.remoteJid) {
        msg.key.remoteJid = jidNormalizedUser(msg.key.remoteJid);
    } else {
        return;
    }

    const rjid = msg.key.remoteJid;

    if (rjid.endsWith("@g.us")) return;
    if (rjid.endsWith("@newsletter")) return;
    if (rjid.endsWith("status@broadcast")) return;

      let textoMensagem = "";
      if (msg.message.conversation) {
          textoMensagem = msg.message.conversation;
      } else if (msg.message.extendedTextMessage?.text) {
          textoMensagem = msg.message.extendedTextMessage.text;
      } else if (msg.message.imageMessage) {
          textoMensagem = "[Imagem recebida]"
      } else if (msg.message.videoMessage) {
          textoMensagem = "[Video recebido]"
      } else if (msg.message.stickerMessage) {
          textoMensagem = "[Sticker recebido]"
      }

    const sender = msg.key.remoteJid
    const senderNumber = rjid.replace(/@s\.whatsapp\.net$/, "") || "";

    const zona = "America/Sao_Paulo";
    const usuario = await Usuario.getUsuarioByNumber(senderNumber);
    addMensagem({
      id: msg.key.id,
      from: senderNumber,
      nome: msg.pushName || "Desconhecido",
      status: usuario?.atividade || "Desconhecido",
      papel: usuario?.papel || "Desconhecido",
      texto: textoMensagem || "",
      data : moment().tz(zona).format("DD-MM-YYYY"),
      hora : moment().tz(zona).format("HH:mm")
    });
  });
}

export function getSock() {
  return globalSock;
}