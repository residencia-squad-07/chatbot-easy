import { Request, Response } from "express";
import userAdmService from "../services/userAdmService";

const getAllUserAdm = async (req: Request, res: Response) => {
    const { status, message } = await userAdmService.getAllUserAdm();
    return res.status(status).json(message);
};

const getUserAdmById = async (req: Request, res: Response) => {
    const id = Number(req.params.id);
    const { type, status, message } = await userAdmService.getUserAdmById(id);

    if (type) return res.status(status).json({ message });
    return res.status(status).json(message);
};

const createUserAdm = async (req: Request, res: Response) => {
    const data = req.body;
    const { type, status, message } = await userAdmService.createUserAdm(data);

    if (type) return res.status(status).json({ message });
    return res.status(status).json({ message });
};

const updateUserAdm = async (req: Request, res: Response) => {
    const id = Number(req.params.id);
    const data = req.body;

    const { type, status, message } = await userAdmService.updateUserAdm(id, data);

    if (type) return res.status(status).json({ message });
    return res.status(status).json({ message });
};

const deleteUserAdm = async (req: Request, res: Response) => {
    const id = Number(req.params.id);

    const { type, status, message } = await userAdmService.deleteUserAdm(id);

    if (type) return res.status(status).json({ message });
    return res.status(status).json({ message });
};

export default {
    getAllUserAdm,
    getUserAdmById,
    createUserAdm,
    updateUserAdm,
    deleteUserAdm,
};
