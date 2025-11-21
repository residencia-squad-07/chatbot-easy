import { Router } from "express";
import { listMessage, sendMessage, sendDocument } from "../controllers/messageController";

const router = Router();

router.get("/listar", listMessage);
router.post("/enviar", sendMessage);
router.post("/enviar-documento", sendDocument);

export default router;
