import { Router } from "express";
import agendController from "../controllers/agendController";
const router = Router();

router.get("/lagend", agendController.getAllAgend);
router.get("/gagend/:id", agendController.getAgendById);
router.post("/cagend", agendController.createAgend);
router.put("/uagend/:id", agendController.updateAgend);
router.put("/pproxecec/:id", agendController.updateAgendProxExec);
router.delete("/dagend/:id", agendController.deleteAgend);
router.get("/gagendui/:id_user", agendController.getAgendByUserId);

export default router;