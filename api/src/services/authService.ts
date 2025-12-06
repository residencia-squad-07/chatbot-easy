import authModel from '../models/authModel';

const login = async (email: string, senha: string, role: string) => {

    if (role === "plataforma_admin") {

        const adm = await authModel.getAdminByEmail(email);
        if (!adm) return null;

        // senha NÃO criptografada → comparar texto puro
        if (senha !== adm.senha) return null;

        return {
            id: adm.id_admin,
            nome: adm.nome,
            email: adm.email,
            role: "plataforma_admin",
            idEmpresa: null
        };
    }

    if (role === "empresa_admin") {

        const userAdm = await authModel.getUserAdmByEmail(email);
        if (!userAdm) return null;

        // senha NÃO criptografada → comparar texto puro
        if (senha !== userAdm.senha) return null;

        return {
            id: userAdm.id_user_admin,
            nome: userAdm.nome,
            email: userAdm.email,
            role: "empresa_admin",
            idEmpresa: userAdm.id_empresa
        };
    }

    return null;
};

export default { login };
