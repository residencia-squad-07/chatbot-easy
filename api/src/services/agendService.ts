import agendModel, { Agendamentos } from "../models/agendModel";
import schemas from "./validations/schemas";

const getAllAgend = async () => {
  const AllAgend = await agendModel.getAllAgend ();
  return {
    type: null,
    message: AllAgend,
    status: 200
  }
}

const getAgendById = async (id: number) => {
  const agendById = await agendModel.getAgendById(id);
  if(!agendById) {
    return {
      type: 'error',
      message: 'Agendamento não foi encontrado',
      status: 404
    }
  }
  return {
    type: null,
    message: agendById,
    status: 200
  }
}

const createAgend = async (agend: Agendamentos) => {
  const validateAgend = schemas.agendSchema.validate(agend);
  if(validateAgend.error) {
    return {
    type: 'error',
    message: validateAgend.error.details[0].message,
    status: 422
    }
  }
  const insertIdAgend = await agendModel.createAgend(agend);
  return {
    type: null,
    message: `Agendamento inserido com sucesso no id: ${ insertIdAgend }`,
    status: 200
  }
}

const updateAgend = async (id: number, agend: Partial<Agendamentos>) => {
  const agendExists = await agendModel.getAgendById(id);
  if(!agendExists) {
    return {
      type: 'error',
      message: 'Agendamento não foi encontrado',
      status: 404
    }
  }

  const validateAgend = schemas.agendSchema.validate(agend);
  if(validateAgend.error) {
    return {
    type: 'error',
    message: validateAgend.error.details[0].message,
    status: 422
    }
  }

  await agendModel.updateAgendById(id, agend);
  return {
    type: null,
    message: 'Agendamento atualizado com sucesso',
    status: 201
  } 
}

const deleteAgend = async (id: number) => {
  const agendExists = await agendModel.getAgendById(id);
  if(!agendExists) {
    return {
      type: 'error',
      message: 'Agendamento não foi encontrado',
      status: 404
    }
  }

  await agendModel.deleteAgend(id);
  return {
    type: null,
    message: "Agendamento deletado com sucesso",
    status: 200
  }
}

export default {
  getAllAgend,
  getAgendById,
  createAgend,
  updateAgend,
  deleteAgend
}