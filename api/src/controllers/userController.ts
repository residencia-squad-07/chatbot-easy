import { Request, Response } from "express";
import userService from "../services/userService";

const criarUsuario = async (req: Request, res: Response) => {
    const user = req.body;
    const { type, message, status } = await userService.createUsuario(user);
    if (type) {
        return res.status(status).json({ message });
    }
    return res.status(201).json({ message });
}

const listarUsuarios = async (req: Request, res: Response) => {
    const { type, message, status } = await userService.getAllUser();
    if (type) {
        return res.status(status).json({ message });
    }
    return res.status(200).json({ message });
}

const updateUsuario = async (req: Request, res: Response) => {
    const { id } = req.params;
    const nId = Number(id)
    const usuario = req.body;
    const { type, message, status } = await userService.updateUsuario(nId, usuario);
    if (type) {
        return res.status(status).json({ message });
    }
    return res.status(201).json({ message });
}

const deleteUsuario = async (req: Request, res: Response) => {
    const { id } = req.params;
    const nId = Number(id)
    const { type, message, status } = await userService.deleteUsuario(nId);
    if (type) {
        return res.status(status).json({ message });
    }
    return res.status(201).json({ message });
}

const getUsuarioById = async (req: Request, res: Response) => {
    const { id } = req.params;
    const nId = Number(id)
    const { type, message, status } = await userService.getUserById(nId);
    if (type) {
        return res.status(status).json({ message })
    }
    return res.status(status).json(message)
}

const getUsuariosByEmp = async (req: Request, res: Response) => {
  try {
    const { id_empresa } = req.params;
    const nIdEmpresa = Number(id_empresa);
    const { type, message, status } = await userService.getUsuariosByEmp(nIdEmpresa);
if (type) {
  return res.status(status).json({ message });
}
return res.status(status).json({ data: message });

  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Erro interno no servidor' });
  }
};

const getUsuarioByTelefone = async (req: Request, res: Response) => {
  try {
    const { telefone } = req.params;
    console.log(`[DEBUG] Buscando usuário com telefone: ${telefone}`);
    const { type, message, status } = await userService.getUsuarioByTelefone(telefone);
    if (type) {
      console.log(`[DEBUG] Usuário não encontrado: ${message}`);
      return res.status(status).json({ message });
    }
    console.log(`[DEBUG] Usuário encontrado:`, message);
    // Always return with consistent format: { user: {...} } or { message: {...} }
    return res.status(status).json(message);
  } catch (error) {
    console.error(`[ERROR] Exception in getUsuarioByTelefone:`, error);
    return res.status(500).json({ message: 'Erro interno no servidor' });
  }
};

const marcarPrimeiroContato = async (req: Request, res: Response) => {
  try {
    const { telefone } = req.params;

    const { type, message, status } = await userService.marcarPrimeiroContato(telefone);

    if (type) {
      return res.status(status).json({ message });
    }

    return res.status(status).json({ message });
  } catch (error) {
    console.error('[ERROR] marcarPrimeiroContato:', error);
    return res.status(500).json({ message: 'Erro interno no servidor' });
  }
}

const marcarUsuarioInativo = async (req: Request, res: Response) => {
  const { id } = req.params;
  const nId = Number(id);

  const { type, message, status } = await userService.marcarUsuarioInativo(nId);

  if (type) {
    return res.status(status).json({ message });
  }

  return res.status(status).json({ message });
};


export default {
    criarUsuario,
    listarUsuarios,
    updateUsuario,
    deleteUsuario,
    getUsuarioById,
    getUsuariosByEmp,
    getUsuarioByTelefone,
    marcarPrimeiroContato,
    marcarUsuarioInativo
    
}