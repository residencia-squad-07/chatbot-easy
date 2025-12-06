import { Request, Response } from "express";
import authService from "../services/authService";

const login = async (req: Request, res: Response) => {
    const { email, senha, role } = req.body;

    const user = await authService.login(email, senha, role);

    if (!user) {
        return res.status(401).json({ message: "Credenciais inválidas" });
    }

    console.log("🔵 Usuário vindo do service:", user);

    const idEmpresaFinal: number | null =
        user.idEmpresa !== undefined
            ? Number(user.idEmpresa)
            : user.idEmpresa !== undefined
                ? Number(user.idEmpresa)
                : null;

    return res.status(200).json({
        id: user.id,
        nome: user.nome,
        email: user.email,
        role: user.role,
        idEmpresa: idEmpresaFinal
    });
};

export default { login };
