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

const getAgendByUserId = async (req: Request, res: Response) => {
  const { id_user } = req.params;
  const nId = Number(id_user)
  const { type, message, status } = await agendService.getAgendByUserId(nId);
  if (type) {
  return res.status(status).json({ message })
  }
  return res.status(status).json(message)
}

const createAgend = async (req: Request, res: Response) => { 
  console.log("BODY RECEBIDO PELO NODE:", JSON.stringify(req.body, null, 2));
  const agend = req.body;
  const { type, message, status } = await agendService.createAgend(agend);
  if (type) {
    return res.status(status).json({ message });
  }
  return res.status(201).json({ message });
}

const updateAgend = async (req: Request, res: Response) => {
  const { id } = req.params;
  const nId = Number(id)
  const agend = req.body;
  const { type, message, status } = await agendService.updateAgend(nId, agend);
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
  getAgendByUserId,
  createAgend,
  updateAgend,
  deleteAgend
}