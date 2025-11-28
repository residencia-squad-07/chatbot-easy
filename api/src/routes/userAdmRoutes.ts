import { Router } from "express";
import userAdmController from "../controllers/userAdmController";

const router = Router();

router.get("/luseradm", userAdmController.getAllUserAdm);
router.get("/luseradm/:id", userAdmController.getUserAdmById);
router.post("/cuseradm", userAdmController.createUserAdm);
router.put("/uuseradm/:id", userAdmController.updateUserAdm);
router.delete("/duseradm/:id", userAdmController.deleteUserAdm);

export default router;
