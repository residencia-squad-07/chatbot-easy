import userAdmModel from "../models/userAdmModel";
import schemas from "./validations/schemas";
import type { UserAdm } from '../models/userAdmModel';

const getAllUserAdm = async () => {
    const all = await userAdmModel.getAllUserAdm();
    return {
        type: null,
        message: all,
        status: 200
    };
};

const getUserAdmById = async (id: number) => {
    const userAdm = await userAdmModel.getUserAdmById(id);
    if (!userAdm) {
        return {
            type: 'error',
            message: 'User_Admin não encontrado',
            status: 404
        };
    }
    return {
        type: null,
        message: userAdm,
        status: 200
    };
};

const createUserAdm = async (data: UserAdm) => {
    const validation = schemas.userAdmSchema.validate(data);

    if (validation.error) {
        return {
            type: 'error',
            message: validation.error.details[0].message,
            status: 422
        };
    }

    const insertId = await userAdmModel.createUserAdm(data);
    return {
        type: null,
        message: `User_Admin criado com sucesso no id: ${insertId}`,
        status: 201
    };
};

const updateUserAdm = async (id: number, data: Partial<UserAdm>) => {
    const exists = await userAdmModel.getUserAdmById(id);
    if (!exists) {
        return {
            type: 'error',
            message: 'User_Admin não encontrado',
            status: 404
        };
    }

    const validation = schemas.userAdmSchema.validate(data);
    if (validation.error) {
        return {
            type: 'error',
            message: validation.error.details[0].message,
            status: 422
        };
    }

    await userAdmModel.updateUserAdm(id, data);
    return {
        type: null,
        message: 'User_Admin atualizado com sucesso',
        status: 201
    };
};

const deleteUserAdm = async (id: number) => {
    const exists = await userAdmModel.getUserAdmById(id);

    if (!exists) {
        return {
            type: 'error',
            message: 'User_Admin não encontrado',
            status: 404
        };
    }

    await userAdmModel.deleteUserAdm(id);
    return {
        type: null,
        message: "User_Admin deletado com sucesso",
        status: 200
    };
};

export default {
    getAllUserAdm,
    getUserAdmById,
    createUserAdm,
    updateUserAdm,
    deleteUserAdm
};
