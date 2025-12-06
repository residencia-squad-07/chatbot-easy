import { Router } from "express";
import empresaController from "../controllers/empresaController";

const router = Router();

router.get("/listarEmpresa", empresaController.getAllEmpresa);
router.get("/listarempresa/:id", empresaController.getEmpresaById);
router.post("/criarEmpresa", empresaController.createEmpresa);
router.put("/uempresa/:id", empresaController.updateEmpresa);
router.delete("/dempresa", empresaController.deleteEmpresa);

export default router;
