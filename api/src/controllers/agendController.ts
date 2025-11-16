import { Request, Response } from "express";
import agendService from "../services/agendService";

const getAllAgend = async (req: Request, res: Response) => {
  const { message, status } = await agendService.getAllAgend();
  return res.status(status).json(message)
}

const getAgendById = async (req: Request, res: Response) => {
  const { id } = req.params;
  const nId = Number(id)
  const { type, message, status } = await agendService.getAgendById(nId);
  if (type) {
  return res.status(status).json({ message })
  }
  return res.status(status).json(message)
}

const createAgend = async (req: Request, res: Response) => { 
  const configErp = req.body;
  const { type, message, status } = await agendService.createAgend(configErp);
  if (type) {
    return res.status(status).json({ message });
  }
  return res.status(201).json({ message });
}

const updateAgend = async (req: Request, res: Response) => {
  const { id } = req.params;
  const nId = Number(id)
  const userAdmin = req.body;
  const { type, message, status } = await agendService.updateAgend(nId, userAdmin);
  if (type) {
    return res.status(status).json({ message });
  }
  return res.status(201).json({ message });
}

const deleteAgend = async (req: Request, res: Response) => {
  const { id } = req.params;
  const nId = Number(id)
  const { type, message, status } = await agendService.deleteAgend(nId);
  if (type) {
    return res.status(status).json({ message });
  }
  return res.status(201).json({ message });
}

export default {
  getAllAgend,
  getAgendById,
  createAgend,
  updateAgend,
  deleteAgend
}